package com.example.banking.domain;

import com.example.banking.domain.enums.OtpPurpose;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.math.BigDecimal;

import org.hibernate.type.NumericBooleanConverter;

@Entity
@Table(name = "otp_challenges")
public class OtpChallenge {

    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private String id;

    @Column(name = "reference_id", nullable = false, unique = true, length = 36)
    private String referenceId;

    @Column(name = "email", nullable = false, length = 254)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, length = 40)
    private OtpPurpose purpose;

    @Column(name = "otp_hash", nullable = false, length = 64)
    private String otpHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "kyc_reference", length = 36)
    private String kycReference;

    @Column(name = "requested_account_id", length = 100)
    private String requestedAccountId;

    @Column(name = "opening_balance", precision = 19, scale = 4)
    private BigDecimal openingBalance;

    @Column(name = "transaction_account_id", length = 100)
    private String transactionAccountId;

    @Column(name = "destination_account_id", length = 100)
    private String destinationAccountId;

    @Column(name = "transaction_operation", length = 20)
    private String transactionOperation;

    @Column(name = "transaction_amount", precision = 19, scale = 4)
    private BigDecimal transactionAmount;

    @Convert(converter = NumericBooleanConverter.class)
    @Column(name = "consumed", nullable = false)
    private boolean consumed;

    protected OtpChallenge() {
        // Required by JPA.
    }

    public OtpChallenge(
            String id,
            String referenceId,
            String email,
            OtpPurpose purpose,
            String otpHash,
            Instant expiresAt) {
        this.id = id;
        this.referenceId = referenceId;
        this.email = email;
        this.purpose = purpose;
        this.otpHash = otpHash;
        this.expiresAt = expiresAt;
        this.attempts = 0;
        this.consumed = false;
    }

    public OtpChallenge(
            String id,
            String referenceId,
            String email,
            OtpPurpose purpose,
            String otpHash,
            Instant expiresAt,
            String kycReference,
            String requestedAccountId,
            BigDecimal openingBalance) {
        this(id, referenceId, email, purpose, otpHash, expiresAt);
        this.kycReference = kycReference;
        this.requestedAccountId = requestedAccountId;
        this.openingBalance = openingBalance;
    }

    public OtpChallenge(
            String id,
            String referenceId,
            String email,
            OtpPurpose purpose,
            String otpHash,
            Instant expiresAt,
            String transactionAccountId,
            String destinationAccountId,
            String transactionOperation,
            BigDecimal transactionAmount) {
        this(id, referenceId, email, purpose, otpHash, expiresAt);
        this.transactionAccountId = transactionAccountId;
        this.destinationAccountId = destinationAccountId;
        this.transactionOperation = transactionOperation;
        this.transactionAmount = transactionAmount;
    }

    public String getTransactionAccountId() { return transactionAccountId; }
    public String getDestinationAccountId() { return destinationAccountId; }
    public String getTransactionOperation() { return transactionOperation; }
    public BigDecimal getTransactionAmount() { return transactionAmount; }

    public String getKycReference() { return kycReference; }
    public String getRequestedAccountId() { return requestedAccountId; }
    public BigDecimal getOpeningBalance() { return openingBalance; }

    public String getId() { return id; }
    public String getReferenceId() { return referenceId; }
    public String getEmail() { return email; }
    public OtpPurpose getPurpose() { return purpose; }
    public String getOtpHash() { return otpHash; }
    public Instant getExpiresAt() { return expiresAt; }
    public int getAttempts() { return attempts; }
    public boolean isConsumed() { return consumed; }

    public void incrementAttempts() { attempts++; }
    public void consume() { consumed = true; }
}
