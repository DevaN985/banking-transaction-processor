package com.example.banking.dto.response;

public record AccountOtpStartedResponse(
        String challengeId, String accountId, String status, String maskedEmail, String message) {}
