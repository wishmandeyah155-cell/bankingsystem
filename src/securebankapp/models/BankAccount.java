package securebankapp.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BankAccount {
    private final String accountNumber;
    private final String username;
    private BigDecimal balance;
    private final String currency;
    private final LocalDateTime createdAt;

    public BankAccount(String accountNumber, String username, BigDecimal balance, String currency, LocalDateTime createdAt) {
        this.accountNumber = accountNumber;
        this.username = username;
        this.balance = balance;
        this.currency = currency;
        this.createdAt = createdAt;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getUsername() {
        return username;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
