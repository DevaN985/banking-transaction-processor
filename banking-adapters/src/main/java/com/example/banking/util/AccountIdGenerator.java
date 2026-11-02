package com.example.banking.util;

import com.example.banking.service.TransactionService;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/** Generates unique 15-digit account identifiers using the account service for collision checks. */
@Component
public class AccountIdGenerator {
    private static final long ACCOUNT_ID_BASE = 100_000_000_000_000L;
    private static final long ACCOUNT_ID_RANGE = 900_000_000_000_000L;
    private static final int MAX_GENERATION_ATTEMPTS = 10;

    private final TransactionService transactionService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AccountIdGenerator(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public String generate() {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String candidate = Long.toString(
                    ACCOUNT_ID_BASE + secureRandom.nextLong(ACCOUNT_ID_RANGE));
            if (!transactionService.accountExists(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to generate a unique account ID. Please retry.");
    }
}
