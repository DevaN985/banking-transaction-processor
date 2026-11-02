package com.example.banking.domain;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity
@Table(name = "accounts")
public class Account  {
    @Id
    @Column(name = "account_id", nullable = false, updatable = false, length = 100)
    private String id;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;
    @Column(name = "customer_id", length = 36)
    private String customerId;
    protected Account()  {
    }
    public Account(String id, BigDecimal openingBalance)  {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Account ID is required");
        validateNonNegative(openingBalance, "Opening balance cannot be negative");
        this.id = id;
        this.balance = openingBalance;
    }
    public Account(String id, BigDecimal openingBalance, String customerId) {
        this(id, openingBalance);
        if (customerId == null || customerId.isBlank()) throw new IllegalArgumentException("Customer ID is required");
        this.customerId = customerId;
    }
    public String customerId() { return customerId; }
    public String id()  {
        return id;
    }
    public BigDecimal balance()  {
        return balance;
    }
    public void deposit(BigDecimal amount)  {
        validatePositive(amount);
        balance = balance.add(amount);
    }
    public void withdraw(BigDecimal amount)  {
        validatePositive(amount);
        if (balance.compareTo(amount) < 0) throw new IllegalStateException("Insufficient funds");
        balance = balance.subtract(amount);
    }
    private static void validatePositive(BigDecimal amount)  {
        validateNonNegative(amount, "Amount must be greater than zero");
        if (amount.signum() == 0) throw new IllegalArgumentException("Amount must be greater than zero");
    }
    private static void validateNonNegative(BigDecimal amount, String message)  {
        if (amount == null || amount.signum() < 0) throw new IllegalArgumentException(message);
    }
}
