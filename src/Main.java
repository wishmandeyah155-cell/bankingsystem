import securebankapp.models.BankAccount;
import securebankapp.models.Transaction;
import securebankapp.models.User;
import securebankapp.services.AuthenticationService;
import securebankapp.services.BankingService;
import securebankapp.storage.FileStorageService;
import securebankapp.utils.InputValidator;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            Path dataDirectory = Paths.get("data");
            FileStorageService storageService = new FileStorageService(dataDirectory);
            storageService.ensureDataDirectoryExists();

            AuthenticationService authenticationService = new AuthenticationService(storageService, dataDirectory.resolve("users.txt"));
            BankingService bankingService = new BankingService(storageService, dataDirectory.resolve("accounts.txt"), dataDirectory.resolve("transactions.txt"));

            authenticationService.loadUsers();
            bankingService.loadAccounts();
            bankingService.loadTransactions();

            Scanner scanner = new Scanner(System.in);
            showMainMenu(scanner, authenticationService, bankingService);
        } catch (IOException e) {
            System.out.println("Application failed to initialize storage. Please check file access permissions.");
        }
    }

    private static void showMainMenu(Scanner scanner, AuthenticationService auth, BankingService banking) {
        while (true) {
            System.out.println();
            System.out.println("WELCOME TO SECURE BANKING APPLICATION");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine();
            if (!InputValidator.isValidMenuChoice(choice, 1, 3)) {
                System.out.println("Invalid option. Please choose 1, 2, or 3.");
                continue;
            }

            int option = Integer.parseInt(choice);
            switch (option) {
                case 1 -> handleRegistration(scanner, auth);
                case 2 -> {
                    User user = handleLogin(scanner, auth);
                    if (user != null) {
                        showDashboard(scanner, user, auth, banking);
                    }
                }
                case 3 -> {
                    System.out.println("Goodbye. Your banking session is closed.");
                    return;
                }
                default -> System.out.println("Invalid selection.");
            }
        }
    }

    private static void handleRegistration(Scanner scanner, AuthenticationService auth) {
        try {
            System.out.print("Enter username: ");
            String username = scanner.nextLine();
            System.out.print("Enter password: ");
            String password = scanner.nextLine();
            User user = auth.register(username, password);
            System.out.println("Registration successful for user: " + user.getUsername());
        } catch (IllegalArgumentException e) {
            System.out.println("Registration failed: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Registration failed because the user file could not be saved safely.");
        }
    }

    private static User handleLogin(Scanner scanner, AuthenticationService auth) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        User user = auth.login(username, password);
        if (user == null) {
            System.out.println("Login failed. Check your username and password.");
            return null;
        }
        System.out.println("Login successful. Welcome, " + user.getUsername() + ".");
        return user;
    }

    private static void showDashboard(Scanner scanner, User user, AuthenticationService auth, BankingService banking) {
        while (true) {
            System.out.println();
            System.out.println("SECURE BANKING DASHBOARD");
            System.out.println("1. Create Bank Account");
            System.out.println("2. View My Accounts");
            System.out.println("3. Check Account Balance");
            System.out.println("4. Deposit Funds");
            System.out.println("5. Withdraw Funds");
            System.out.println("6. View Transaction History");
            System.out.println("7. Logout");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine();
            if (!InputValidator.isValidMenuChoice(choice, 1, 7)) {
                System.out.println("Invalid option. Please try again.");
                continue;
            }

            int option = Integer.parseInt(choice);
            try {
                switch (option) {
                    case 1 -> createAccount(scanner, user, banking);
                    case 2 -> viewAccounts(user, banking);
                    case 3 -> checkBalance(scanner, user, banking);
                    case 4 -> depositFunds(scanner, user, banking);
                    case 5 -> withdrawFunds(scanner, user, banking);
                    case 6 -> viewTransactionHistory(scanner, user, banking);
                    case 7 -> {
                        System.out.println("Logging out...");
                        return;
                    }
                    default -> System.out.println("Invalid selection.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Operation failed: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("A storage error occurred while saving your request.");
            }
        }
    }

    private static void createAccount(Scanner scanner, User user, BankingService banking) throws IOException {
        BankAccount account = banking.createAccount(user.getUsername());
        System.out.println("Account created successfully: " + account.getAccountNumber());
    }

    private static void viewAccounts(User user, BankingService banking) {
        List<BankAccount> accounts = banking.getAccountsForUser(user.getUsername());
        if (accounts.isEmpty()) {
            System.out.println("You do not have any bank accounts yet.");
            return;
        }
        for (BankAccount account : accounts) {
            System.out.println("Account: " + account.getAccountNumber() + " | Balance: " + account.getBalance() + " " + account.getCurrency() + " | Created: " + account.getCreatedAt());
        }
    }

    private static void checkBalance(Scanner scanner, User user, BankingService banking) {
        List<BankAccount> accounts = banking.getAccountsForUser(user.getUsername());
        if (accounts.isEmpty()) {
            System.out.println("No accounts available to check.");
            return;
        }
        System.out.print("Enter account number: ");
        String accountNumber = scanner.nextLine();
        BankAccount account = banking.getAccountByNumber(accountNumber, user.getUsername());
        if (account == null) {
            System.out.println("Account not found or access denied.");
            return;
        }
        System.out.println("Current balance: " + account.getBalance() + " " + account.getCurrency());
    }

    private static void depositFunds(Scanner scanner, User user, BankingService banking) throws IOException {
        List<BankAccount> accounts = banking.getAccountsForUser(user.getUsername());
        if (accounts.isEmpty()) {
            System.out.println("You need an account before making a deposit.");
            return;
        }
        System.out.print("Enter account number: ");
        String accountNumber = scanner.nextLine();
        BankAccount account = banking.getAccountByNumber(accountNumber, user.getUsername());
        if (account == null) {
            System.out.println("Account not found or access denied.");
            return;
        }
        System.out.print("Enter deposit amount: ");
        String amountText = scanner.nextLine();
        if (!InputValidator.isValidAmountText(amountText)) {
            System.out.println("Deposit rejected: invalid amount.");
            return;
        }
        BigDecimal amount = new BigDecimal(amountText.trim());
        boolean success = banking.deposit(user.getUsername(), accountNumber, amount);
        if (success) {
            System.out.println("Deposit successful.");
        } else {
            System.out.println("Deposit rejected: amount must be greater than zero and within valid currency precision.");
        }
    }

    private static void withdrawFunds(Scanner scanner, User user, BankingService banking) throws IOException {
        List<BankAccount> accounts = banking.getAccountsForUser(user.getUsername());
        if (accounts.isEmpty()) {
            System.out.println("No account available for withdrawal.");
            return;
        }
        System.out.print("Enter account number: ");
        String accountNumber = scanner.nextLine();
        BankAccount account = banking.getAccountByNumber(accountNumber, user.getUsername());
        if (account == null) {
            System.out.println("Account not found or access denied.");
            return;
        }
        System.out.print("Enter withdrawal amount: ");
        String amountText = scanner.nextLine();
        if (!InputValidator.isValidAmountText(amountText)) {
            System.out.println("Withdrawal rejected: invalid amount.");
            return;
        }
        BigDecimal amount = new BigDecimal(amountText.trim());
        boolean success = banking.withdraw(user.getUsername(), accountNumber, amount);
        if (success) {
            System.out.println("Withdrawal successful.");
        } else {
            System.out.println("Withdrawal rejected: insufficient funds or invalid amount.");
        }
    }

    private static void viewTransactionHistory(Scanner scanner, User user, BankingService banking) {
        List<BankAccount> accounts = banking.getAccountsForUser(user.getUsername());
        if (accounts.isEmpty()) {
            System.out.println("You have no accounts to review.");
            return;
        }
        System.out.print("Enter account number: ");
        String accountNumber = scanner.nextLine();
        BankAccount account = banking.getAccountByNumber(accountNumber, user.getUsername());
        if (account == null) {
            System.out.println("Account not found or access denied.");
            return;
        }
        List<Transaction> history = banking.getTransactionsForAccount(user.getUsername(), accountNumber);
        if (history.isEmpty()) {
            System.out.println("No completed transactions for this account.");
            return;
        }
        for (Transaction transaction : history) {
            System.out.println(transaction.getType() + " | " + transaction.getAmount() + " | " + transaction.getTimestamp() + " | Balance after: " + transaction.getBalanceAfterTransaction());
        }
    }
}
