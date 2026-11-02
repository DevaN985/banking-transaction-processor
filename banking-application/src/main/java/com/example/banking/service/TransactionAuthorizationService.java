package com.example.banking.service;

import com.example.banking.domain.OtpChallenge;
import com.example.banking.dto.request.TransactionOtpConfirmRequest;
import com.example.banking.dto.request.TransactionOtpRequest;
import com.example.banking.domain.enums.OtpPurpose;
import com.example.banking.dto.request.TransactionOtpResponse;
import com.example.banking.notification.EmailProvider;
import com.example.banking.repository.OtpChallengeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.CustomerRepository;

@Service
public class TransactionAuthorizationService {
    private static final BigDecimal OTP_THRESHOLD = new BigDecimal("1000.00");
    private final OtpChallengeRepository challenges;
    private final EmailProvider email;
    private final TransactionService transactions;
    private final AccountRepository accounts;
    private final CustomerRepository customers;

    public TransactionAuthorizationService(
            OtpChallengeRepository challenges,
            EmailProvider email,
            TransactionService transactions,
            AccountRepository accounts,
            CustomerRepository customers) {

        this.challenges = challenges;
        this.email = email;
        this.transactions = transactions;
        this.accounts = accounts;
        this.customers = customers;
    }

    public boolean requiresOtp(BigDecimal amount) {
        return amount != null && amount.compareTo(OTP_THRESHOLD) > 0;
    }

    public TransactionOtpResponse requestOtp(TransactionOtpRequest r) {
        if (!requiresOtp(r.getAmount())) {
            throw new IllegalArgumentException("OTP is required only for transactions above INR 1,000");
        }

        var account = accounts.findById(r.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        String customerId = account.customerId();
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer is not associated with this account");
        }
        var customer = customers.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        String registeredEmail = customer.getEmail();
        if (registeredEmail == null || registeredEmail.isBlank()) {
            throw new IllegalArgumentException("Registered customer email not found");
        }

        if (r.getOperation() == TransactionOtpRequest.Operation.TRANSFER) {
            if (r.getDestinationAccountId() == null || r.getDestinationAccountId().isBlank()) {
                throw new IllegalArgumentException("Destination account is required for transfer");
            }
            if (r.getAccountId().equals(r.getDestinationAccountId())) {
                throw new IllegalArgumentException("Source and destination must be different accounts");
            }
            if (!accounts.existsById(r.getDestinationAccountId())) {
                throw new IllegalArgumentException("Destination account not found");
            }
        } else if (r.getDestinationAccountId() != null) {
            throw new IllegalArgumentException("Destination account is not applicable to withdrawal");
        }

        String reference = UUID.randomUUID().toString();
        String otp = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        OtpPurpose purpose = r.getOperation() == TransactionOtpRequest.Operation.TRANSFER
                ? OtpPurpose.TRANSFER_AUTHORIZATION
                : OtpPurpose.WITHDRAWAL_AUTHORIZATION;

        challenges.save(new OtpChallenge(
                reference, reference, registeredEmail, purpose, hash(otp),
                Instant.now().plusSeconds(300), r.getAccountId(),
                r.getDestinationAccountId(), r.getOperation().name(), r.getAmount()));

        email.sendEmail(
                registeredEmail,
                "Banking transaction confirmation OTP",
                "Your OTP is " + otp + ". It expires in 5 minutes. Transaction: "
                        + r.getOperation() + ", amount INR " + r.getAmount() + ".");

        return new TransactionOtpResponse(
                reference, r.getAccountId(), r.getOperation().name(), r.getAmount(),
                "OTP_SENT", "OTP sent to your registered email");
    }

    @Transactional
    public String confirm(TransactionOtpConfirmRequest r) {
        OtpChallenge c = challenges.findById(r.getChallengeId())
                .orElseThrow(() -> new IllegalArgumentException("OTP challenge not found"));
        if (!c.getReferenceId().equals(r.getChallengeId())) {
            throw new IllegalArgumentException("OTP challenge does not match request");
        }
        if (c.getTransactionOperation() == null || c.getTransactionAccountId() == null
                || c.getTransactionAmount() == null) {
            throw new IllegalArgumentException("OTP challenge does not contain transaction details");
        }
        if (c.isConsumed() || Instant.now().isAfter(c.getExpiresAt())) {
            throw new IllegalStateException("OTP expired or already used");
        }
        if (c.getAttempts() >= 5) {
            throw new IllegalStateException("Maximum OTP attempts exceeded");
        }
        if (!MessageDigest.isEqual(c.getOtpHash().getBytes(StandardCharsets.UTF_8),
                hash(r.getOtp()).getBytes(StandardCharsets.UTF_8))) {
            c.incrementAttempts();
            challenges.save(c);
            throw new IllegalArgumentException("Invalid OTP");
        }

        TransactionOtpRequest.Operation operation =
                TransactionOtpRequest.Operation.valueOf(c.getTransactionOperation());
        if (operation == TransactionOtpRequest.Operation.TRANSFER) {
            if (c.getPurpose() != OtpPurpose.TRANSFER_AUTHORIZATION) {
                throw new IllegalArgumentException("OTP purpose does not match transaction");
            }
            if (c.getDestinationAccountId() == null || c.getDestinationAccountId().isBlank()) {
                throw new IllegalArgumentException("Destination account is required");
            }
            String result = transactions.transfer(c.getTransactionAccountId(),
                    c.getDestinationAccountId(), c.getTransactionAmount()).toString();
            c.consume();
            challenges.save(c);
            return result;
        }

        if (c.getPurpose() != OtpPurpose.WITHDRAWAL_AUTHORIZATION) {
            throw new IllegalArgumentException("OTP purpose does not match transaction");
        }
        String result = transactions.withdraw(c.getTransactionAccountId(),
                c.getTransactionAmount()).transactionId();
        c.consume();
        challenges.save(c);
        return result;
    }

    private static String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
