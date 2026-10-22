package securebankapp.services;

import securebankapp.models.BankAccount;
import securebankapp.models.Transaction;
import securebankapp.storage.FileStorageService;
import securebankapp.utils.InputValidator;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class BankingService {
    public static final BigDecimal ZERO = new BigDecimal("0.00");
    public static final int SCALE = 2;
    private final FileStorageService storageService;
    private final Path accountFile;
    private final Path transactionFile;
    private final List<BankAccount> accounts;
    private final List<Transaction> transactions;

    public BankingService(FileStorageService storageService, Path accountFile, Path transactionFile) {
        this.storageService = storageService;
        this.accountFile = accountFile;
        this.transactionFile = transactionFile;
        this.accounts = new ArrayList<>();
        this.transactions = new ArrayList<>();
    }

    public BankAccount createAccount(String username) throws IOException {
        String cleanUsername = InputValidator.sanitizeText(username);
        if (!InputValidator.isValidUsername(cleanUsername)) {
            throw new IllegalArgumentException("Invalid username.");
        }
        String accountNumber = generateAccountNumber();
        BankAccount account = new BankAccount(accountNumber, cleanUsername, ZERO, "USD", LocalDateTime.now());
        accounts.add(account);
        saveAccounts();
        return account;
    }

    public List<BankAccount> getAccountsForUser(String username) {
        List<BankAccount> result = new ArrayList<>();
        for (BankAccount account : accounts) {
            if (Objects.equals(account.getUsername(), username)) {
                result.add(account);
            }
        }
        return result;
    }

    public BankAccount getAccountByNumber(String accountNumber, String username) {
        for (BankAccount account : accounts) {
            if (Objects.equals(account.getAccountNumber(), accountNumber) && Objects.equals(account.getUsername(), username)) {
                return account;
            }
        }
        return null;
    }

    public boolean deposit(String username, String accountNumber, BigDecimal amount) throws IOException {
        if (amount == null || !isValidAmount(amount)) {
            return false;
        }
        BankAccount account = getAccountByNumber(accountNumber, username);
        if (account == null) {
            return false;
        }

        BigDecimal roundedAmount = roundMoney(amount);
        BigDecimal newBalance = roundMoney(account.getBalance().add(roundedAmount));
        account.setBalance(newBalance);

        Transaction transaction = new Transaction(UUID.randomUUID().toString(), accountNumber, "DEPOSIT", roundedAmount, LocalDateTime.now(), newBalance);
        transactions.add(transaction);
        saveAccounts();
        saveTransactions();
        return true;
    }

    public boolean withdraw(String username, String accountNumber, BigDecimal amount) throws IOException {
        if (amount == null || !isValidAmount(amount)) {
            return false;
        }
        BankAccount account = getAccountByNumber(accountNumber, username);
        if (account == null) {
            return false;
        }
        if (amount.compareTo(account.getBalance()) > 0) {
            return false;
        }

        BigDecimal roundedAmount = roundMoney(amount);
        BigDecimal newBalance = roundMoney(account.getBalance().subtract(roundedAmount));
        account.setBalance(newBalance);

        Transaction transaction = new Transaction(UUID.randomUUID().toString(), accountNumber, "WITHDRAWAL", roundedAmount, LocalDateTime.now(), newBalance);
        transactions.add(transaction);
        saveAccounts();
        saveTransactions();
        return true;
    }

    public List<Transaction> getTransactionsForAccount(String username, String accountNumber) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (Objects.equals(transaction.getAccountNumber(), accountNumber) && hasAccountAccess(username, accountNumber)) {
                result.add(transaction);
            }
        }
        return result;
    }

    public void loadAccounts() throws IOException {
        accounts.clear();
        List<String> lines = storageService.readLines(accountFile);
        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) {
                continue;
            }
            String[] fields = line.split("\\|", -1);
            if (fields.length != 5) {
                continue;
            }
            try {
                BigDecimal balance = new BigDecimal(fields[2]);
                LocalDateTime createdAt = LocalDateTime.parse(fields[4]);
                accounts.add(new BankAccount(fields[0], fields[1], roundMoney(balance), fields[3], createdAt));
            } catch (Exception ignored) {
                continue;
            }
        }
    }

    public void loadTransactions() throws IOException {
        transactions.clear();
        List<String> lines = storageService.readLines(transactionFile);
        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) {
                continue;
            }
            String[] fields = line.split("\\|", -1);
            if (fields.length != 6) {
                continue;
            }
            try {
                BigDecimal amount = new BigDecimal(fields[3]);
                BigDecimal balance = new BigDecimal(fields[5]);
                LocalDateTime timestamp = LocalDateTime.parse(fields[4]);
                transactions.add(new Transaction(fields[0], fields[1], fields[2], roundMoney(amount), timestamp, roundMoney(balance)));
            } catch (Exception ignored) {
                continue;
            }
        }
    }

    public void saveAccounts() throws IOException {
        storageService.ensureDataDirectoryExists();
        List<String> lines = new ArrayList<>();
        for (BankAccount account : accounts) {
            lines.add(account.getAccountNumber() + "|" + account.getUsername() + "|" + account.getBalance().toPlainString() + "|" + account.getCurrency() + "|" + account.getCreatedAt());
        }
        storageService.writeLines(accountFile, lines);
    }

    public void saveTransactions() throws IOException {
        storageService.ensureDataDirectoryExists();
        List<String> lines = new ArrayList<>();
        for (Transaction transaction : transactions) {
            lines.add(transaction.getTransactionId() + "|" + transaction.getAccountNumber() + "|" + transaction.getType() + "|" + transaction.getAmount().toPlainString() + "|" + transaction.getTimestamp() + "|" + transaction.getBalanceAfterTransaction().toPlainString());
        }
        storageService.writeLines(transactionFile, lines);
    }

    private String generateAccountNumber() {
        return "ACC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private boolean hasAccountAccess(String username, String accountNumber) {
        for (BankAccount account : accounts) {
            if (Objects.equals(account.getAccountNumber(), accountNumber) && Objects.equals(account.getUsername(), username)) {
                return true;
            }
        }
        return false;
    }

    public static BigDecimal roundMoney(BigDecimal value) {
        if (value == null) {
            return ZERO;
        }
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static boolean isValidAmount(BigDecimal amount) {
        return amount != null && amount.compareTo(ZERO) > 0 && amount.scale() <= SCALE;
    }
}
