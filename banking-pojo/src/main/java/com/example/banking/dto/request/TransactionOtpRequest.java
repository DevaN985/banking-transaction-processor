package com.example.banking.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class TransactionOtpRequest {
    public enum Operation {
        WITHDRAWAL, TRANSFER
    }

    @NotNull
    private Operation operation;
    @NotBlank
    @Size(max = 100)
    private String accountId;
    @Size(max = 100)
    private String destinationAccountId;
    @NotNull
    @DecimalMin(value = "0.0001")
    @Digits(integer = 15, fraction = 4)
    private BigDecimal amount;

    public TransactionOtpRequest() {
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation v) {
        operation = v;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String v) {
        accountId = v;
    }

    public String getDestinationAccountId() {
        return destinationAccountId;
    }

    public void setDestinationAccountId(String v) {
        destinationAccountId = v;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal v) {
        amount = v;
    }
}
