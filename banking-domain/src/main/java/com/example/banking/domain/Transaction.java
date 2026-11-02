package com.example.banking.domain;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
@Entity
@Table(name = "transactions", indexes = @Index(name = "idx_tx_account_timestamp", columnList = "account_id,timestamp"))
public class Transaction  {
    @Id
    @Column(name = "transaction_id", nullable = false, updatable = false)
    private String transactionId;
    @Column(name = "account_id", nullable = false, updatable = false, length = 100)
    private String accountId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TransactionType type;
    @Column(nullable = false, precision = 19, scale = 4, updatable = false)
    private BigDecimal amount;
    @Column(nullable = false, updatable = false)
    private Instant timestamp;
    @Column(name = "transfer_id", updatable = false)
    private String transferId;
    protected Transaction()  {
    }
    public Transaction(String transactionId, String accountId, TransactionType type,
    BigDecimal amount, Instant timestamp, String transferId)  {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
        this.transferId = transferId;
    }
    public String transactionId()  {
        return transactionId;
    }
    public String accountId()  {
        return accountId;
    }
    public TransactionType type()  {
        return type;
    }
    public BigDecimal amount()  {
        return amount;
    }
    public Instant timestamp()  {
        return timestamp;
    }
    public String transferId()  {
        return transferId;
    }

    // Standard JavaBean getters are required by Jackson for JSON serialization.
    public String getTransactionId() { return transactionId; }
    public String getAccountId() { return accountId; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public Instant getTimestamp() { return timestamp; }
    public String getTransferId() { return transferId; }
}
