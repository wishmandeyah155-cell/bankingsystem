package securebankapp.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private final String transactionId;
    private final String accountNumber;
    private final String type;
    private final BigDecimal amount;
    private final LocalDateTime timestamp;
    private final BigDecimal balanceAfterTransaction;

    public Transaction(String transactionId, String accountNumber, String type,
                      BigDecimal amount, LocalDateTime timestamp, BigDecimal balanceAfterTransaction) {
        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
        this.balanceAfterTransaction = balanceAfterTransaction;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public BigDecimal getBalanceAfterTransaction() {
        return balanceAfterTransaction;
    }
}
