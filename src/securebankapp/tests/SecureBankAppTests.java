package securebankapp.tests;

import securebankapp.models.BankAccount;
import securebankapp.models.User;
import securebankapp.security.PasswordSecurity;
import securebankapp.services.AuthenticationService;
import securebankapp.services.BankingService;
import securebankapp.storage.FileStorageService;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SecureBankAppTests {
    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("secure-bank-tests");
        Path dataDir = root.resolve("data");
        FileStorageService storage = new FileStorageService(dataDir);

        testSuccessfulRegistration(storage, dataDir);
        testDuplicateUsernameRejection(storage, dataDir);
        testSuccessfulAuthentication(storage, dataDir);
        testIncorrectPasswordRejection(storage, dataDir);
        testUniqueSaltGeneration();
        testAccountOwnershipEnforcement(storage, dataDir);
        testSuccessfulDeposit(storage, dataDir);
        testSuccessfulWithdrawal(storage, dataDir);
        testNegativeDepositRejected(storage, dataDir);
        testNegativeWithdrawalRejected(storage, dataDir);
        testInsufficientFundsRejected(storage, dataDir);
        testBalancePreservedAfterFailedTransaction(storage, dataDir);
        testTransactionHistoryPersistence(storage, dataDir);
        testDataLoadingAfterRestart(storage, dataDir);
        testMalformedStorageRecordHandling(storage, dataDir);

        System.out.println("All SecureBankApp tests passed.");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + " Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void testSuccessfulRegistration(FileStorageService storage, Path dataDir) throws Exception {
        Path userFile = dataDir.resolve("users.txt");
        AuthenticationService auth = new AuthenticationService(storage, userFile);
        User user = auth.register("alice", "StrongPass123!");
        assertTrue(user != null, "User should be created on successful registration.");
        assertEquals("alice", user.getUsername(), "Registered username should match input.");
        assertTrue(!user.getPasswordHash().equals("StrongPass123!"), "Password hash must not equal plaintext password.");
    }

    private static void testDuplicateUsernameRejection(FileStorageService storage, Path dataDir) throws Exception {
        Path userFile = dataDir.resolve("users.txt");
        AuthenticationService auth = new AuthenticationService(storage, userFile);
        auth.loadUsers();
        boolean failed = false;
        try {
            auth.register("alice", "AnotherPass456!");
        } catch (IllegalArgumentException e) {
            failed = true;
        }
        assertTrue(failed, "Duplicate username should be rejected.");
    }

    private static void testSuccessfulAuthentication(FileStorageService storage, Path dataDir) throws Exception {
        Path userFile = dataDir.resolve("users.txt");
        AuthenticationService auth = new AuthenticationService(storage, userFile);
        auth.loadUsers();
        User user = auth.login("alice", "StrongPass123!");
        assertTrue(user != null, "Authentication should succeed for registered user.");
        assertEquals("alice", user.getUsername(), "Authenticated user should be alice.");
    }

    private static void testIncorrectPasswordRejection(FileStorageService storage, Path dataDir) throws Exception {
        Path userFile = dataDir.resolve("users.txt");
        AuthenticationService auth = new AuthenticationService(storage, userFile);
        auth.loadUsers();
        User user = auth.login("alice", "WrongPassword");
        assertTrue(user == null, "Incorrect password should be rejected.");
    }

    private static void testUniqueSaltGeneration() {
        String salt1 = PasswordSecurity.generateSalt();
        String salt2 = PasswordSecurity.generateSalt();
        assertTrue(!salt1.equals(salt2), "Each salt should be unique.");
    }

    private static void testAccountOwnershipEnforcement(FileStorageService storage, Path dataDir) throws Exception {
        Path accountFile = dataDir.resolve("accounts.txt");
        Path transactionFile = dataDir.resolve("transactions.txt");
        BankingService banking = new BankingService(storage, accountFile, transactionFile);
        banking.createAccount("alice");
        BankAccount account = banking.getAccountsForUser("alice").get(0);
        assertTrue(banking.getAccountByNumber(account.getAccountNumber(), "alice") != null, "Owner should access own account.");
        assertTrue(banking.getAccountByNumber(account.getAccountNumber(), "bob") == null, "Other user must not access account.");
    }

    private static void testSuccessfulDeposit(FileStorageService storage, Path dataDir) throws Exception {
        Path accountFile = dataDir.resolve("accounts.txt");
        Path transactionFile = dataDir.resolve("transactions.txt");
        BankingService banking = new BankingService(storage, accountFile, transactionFile);
        banking.createAccount("alice");
        BankAccount account = banking.getAccountsForUser("alice").get(0);
        boolean ok = banking.deposit("alice", account.getAccountNumber(), new BigDecimal("100.00"));
        assertTrue(ok, "Deposit should succeed.");
        assertEquals(0, banking.getAccountByNumber(account.getAccountNumber(), "alice").getBalance().compareTo(new BigDecimal("100.00")), "Deposit balance should be 100.00.");
    }

    private static void testSuccessfulWithdrawal(FileStorageService storage, Path dataDir) throws Exception {
        Path accountFile = dataDir.resolve("accounts.txt");
        Path transactionFile = dataDir.resolve("transactions.txt");
        BankingService banking = new BankingService(storage, accountFile, transactionFile);
        banking.createAccount("alice");
        BankAccount account = banking.getAccountsForUser("alice").get(0);
        banking.deposit("alice", account.getAccountNumber(), new BigDecimal("150.00"));
        boolean ok = banking.withdraw("alice", account.getAccountNumber(), new BigDecimal("50.00"));
        assertTrue(ok, "Withdrawal should succeed for valid amount.");
        assertEquals(0, banking.getAccountByNumber(account.getAccountNumber(), "alice").getBalance().compareTo(new BigDecimal("100.00")), "Withdrawal should leave 100.00 balance.");
    }

    private static void testNegativeDepositRejected(FileStorageService storage, Path dataDir) throws Exception {
        Path accountFile = dataDir.resolve("accounts.txt");
        Path transactionFile = dataDir.resolve("transactions.txt");
        BankingService banking = new BankingService(storage, accountFile, transactionFile);
        banking.createAccount("alice");
        BankAccount account = banking.getAccountsForUser("alice").get(0);
        boolean ok = banking.deposit("alice", account.getAccountNumber(), new BigDecimal("-10.00"));
        assertTrue(!ok, "Negative deposit should be rejected.");
        assertEquals(0, banking.getAccountByNumber(account.getAccountNumber(), "alice").getBalance().compareTo(new BigDecimal("0.00")), "Negative deposit must not change balance.");
    }

    private static void testNegativeWithdrawalRejected(FileStorageService storage, Path dataDir) throws Exception {
        Path accountFile = dataDir.resolve("accounts.txt");
        Path transactionFile = dataDir.resolve("transactions.txt");
        BankingService banking = new BankingService(storage, accountFile, transactionFile);
        banking.createAccount("alice");
        BankAccount account = banking.getAccountsForUser("alice").get(0);
        boolean ok = banking.withdraw("alice", account.getAccountNumber(), new BigDecimal("-10.00"));
        assertTrue(!ok, "Negative withdrawal should be rejected.");
        assertEquals(0, banking.getAccountByNumber(account.getAccountNumber(), "alice").getBalance().compareTo(new BigDecimal("0.00")), "Negative withdrawal must not change balance.");
    }

    private static void testInsufficientFundsRejected(FileStorageService storage, Path dataDir) throws Exception {
        Path accountFile = dataDir.resolve("accounts.txt");
        Path transactionFile = dataDir.resolve("transactions.txt");
        BankingService banking = new BankingService(storage, accountFile, transactionFile);
        banking.createAccount("alice");
        BankAccount account = banking.getAccountsForUser("alice").get(0);
        boolean ok = banking.withdraw("alice", account.getAccountNumber(), new BigDecimal("1.00"));
        assertTrue(!ok, "Insufficient funds should be rejected.");
        assertEquals(0, banking.getAccountByNumber(account.getAccountNumber(), "alice").getBalance().compareTo(new BigDecimal("0.00")), "Insufficient funds must not change balance.");
    }

    private static void testBalancePreservedAfterFailedTransaction(FileStorageService storage, Path dataDir) throws Exception {
        Path accountFile = dataDir.resolve("accounts.txt");
        Path transactionFile = dataDir.resolve("transactions.txt");
        BankingService banking = new BankingService(storage, accountFile, transactionFile);
        banking.createAccount("alice");
        BankAccount account = banking.getAccountsForUser("alice").get(0);
        banking.deposit("alice", account.getAccountNumber(), new BigDecimal("20.00"));
        boolean ok = banking.withdraw("alice", account.getAccountNumber(), new BigDecimal("100.00"));
        assertTrue(!ok, "Failed large withdrawal should be rejected.");
        assertEquals(0, banking.getAccountByNumber(account.getAccountNumber(), "alice").getBalance().compareTo(new BigDecimal("20.00")), "Balance should remain unchanged after failed withdrawal.");
    }

    private static void testTransactionHistoryPersistence(FileStorageService storage, Path dataDir) throws Exception {
        Path accountFile = dataDir.resolve("accounts.txt");
        Path transactionFile = dataDir.resolve("transactions.txt");
        BankingService banking = new BankingService(storage, accountFile, transactionFile);
        banking.createAccount("alice");
        BankAccount account = banking.getAccountsForUser("alice").get(0);
        banking.deposit("alice", account.getAccountNumber(), new BigDecimal("75.00"));
        banking.withdraw("alice", account.getAccountNumber(), new BigDecimal("25.00"));
        List<securebankapp.models.Transaction> transactions = banking.getTransactionsForAccount("alice", account.getAccountNumber());
        assertEquals(2, transactions.size(), "Successful deposit and withdrawal should produce two transactions.");
    }

    private static void testDataLoadingAfterRestart(FileStorageService storage, Path dataDir) throws Exception {
        Path accountFile = dataDir.resolve("accounts.txt");
        Path transactionFile = dataDir.resolve("transactions.txt");
        BankingService first = new BankingService(storage, accountFile, transactionFile);
        first.createAccount("alice");
        BankAccount account = first.getAccountsForUser("alice").get(0);
        first.deposit("alice", account.getAccountNumber(), new BigDecimal("50.00"));
        first.saveAccounts();
        first.saveTransactions();

        BankingService second = new BankingService(storage, accountFile, transactionFile);
        second.loadAccounts();
        second.loadTransactions();
        assertEquals(1, second.getAccountsForUser("alice").size(), "Account list should reload after restart.");
        assertTrue(second.getAccountByNumber(account.getAccountNumber(), "alice") != null, "Account should be reloaded after restart.");
        assertEquals(1, second.getTransactionsForAccount("alice", account.getAccountNumber()).size(), "Transactions should reload after restart.");
    }

    private static void testMalformedStorageRecordHandling(FileStorageService storage, Path dataDir) throws Exception {
        Path userFile = dataDir.resolve("users.txt");
        String malformed = "broken|record\n" + "validuser|hash|salt|120000\n";
        Files.writeString(userFile, malformed);
        AuthenticationService auth = new AuthenticationService(storage, userFile);
        auth.loadUsers();
        assertEquals(1, auth.getUsers().size(), "Malformed storage records should be skipped.");
        assertEquals("validuser", auth.getUsers().get(0).getUsername(), "Valid user record should still load.");
    }
}
