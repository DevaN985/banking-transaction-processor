package com.example.banking.dto.request;

import java.math.BigDecimal;

public class TransactionOtpResponse {

    private String challengeId;
    private String accountId;
    private String operation;
    private BigDecimal amount;
    private String status;
    private String message;

    public TransactionOtpResponse() {
    }

    public TransactionOtpResponse(
            String challengeId,
            String accountId,
            String operation,
            BigDecimal amount,
            String status,
            String message) {

        this.challengeId = challengeId;
        this.accountId = accountId;
        this.operation = operation;
        this.amount = amount;
        this.status = status;
        this.message = message;
    }

    public String getChallengeId() {
        return challengeId;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getOperation() {
        return operation;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}