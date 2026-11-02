package com.example.banking.controller;

import com.example.banking.domain.Account;
import com.example.banking.domain.Customer;
import com.example.banking.domain.GovernmentProof;
import com.example.banking.domain.OtpChallenge;
import com.example.banking.domain.enums.OtpPurpose;
import com.example.banking.dto.request.StartAccountOnboardingRequest;
import com.example.banking.dto.request.ConfirmAccountOnboardingRequest;
import com.example.banking.dto.response.AccountOtpStartedResponse;
import com.example.banking.dto.response.AccountResponse;
import com.example.banking.dto.response.AccountDetailsResponse;
import com.example.banking.dto.response.BalanceResponse;
import com.example.banking.notification.EmailProvider;
import com.example.banking.repository.CustomerRepository;
import com.example.banking.repository.GovernmentProofRepository;
import com.example.banking.repository.OtpChallengeRepository;
import com.example.banking.service.TransactionService;
import com.example.banking.util.AccountIdGenerator;
import com.example.banking.util.OtpUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {
    private static final int OTP_EXPIRY_SECONDS = 300;
    private static final int MAX_OTP_ATTEMPTS = 5;

    private final TransactionService service;
    private final GovernmentProofRepository governmentProofs;
    private final OtpChallengeRepository challenges;
    private final CustomerRepository customers;
    private final EmailProvider email;
    private final AccountIdGenerator accountIdGenerator;

    public AccountController(
            TransactionService service,
            GovernmentProofRepository governmentProofs,
            OtpChallengeRepository challenges,
            CustomerRepository customers,
            EmailProvider email,
            AccountIdGenerator accountIdGenerator) {
        this.service = service;
        this.governmentProofs = governmentProofs;
        this.challenges = challenges;
        this.customers = customers;
        this.email = email;
        this.accountIdGenerator = accountIdGenerator;
    }

    @PostMapping("/onboarding/start")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AccountOtpStartedResponse startOnboarding(
            @Valid @RequestBody StartAccountOnboardingRequest request) {
        String normalizedGovernmentId = request.getGovernmentIdNumber().trim().toUpperCase();
        GovernmentProof proof = governmentProofs
                .findByGovernmentIdTypeAndGovernmentIdNumber(
                        request.getGovernmentIdType(), normalizedGovernmentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "KYC validation failed: government ID not found in trusted records"));

        validateKyc(proof);
        String registeredEmail = proof.getRegisteredEmail();
        if (registeredEmail == null || registeredEmail.isBlank()) {
            throw new IllegalStateException("No registered email is available for this KYC record");
        }
        if (customers.existsByKycReference(proof.getId())) {
            throw new IllegalArgumentException("A customer is already registered for this KYC record");
        }
        if (customers.existsByEmail(registeredEmail)) {
            throw new IllegalArgumentException("A customer is already registered with this email");
        }

        String challengeId = UUID.randomUUID().toString();
        String accountId = accountIdGenerator.generate();
        String otp = OtpUtils.generateOtp();
        OtpChallenge challenge = new OtpChallenge(
                challengeId,
                challengeId,
                registeredEmail,
                OtpPurpose.CUSTOMER_ONBOARDING,
                OtpUtils.hash(otp),
                Instant.now().plusSeconds(OTP_EXPIRY_SECONDS),
                proof.getId(),
                accountId,
                java.math.BigDecimal.ZERO);
        challenges.save(challenge);

        email.sendEmail(
                registeredEmail,
                "Bank account onboarding OTP",
                "Your OTP is " + otp + ". It expires in 5 minutes. Do not share it.");

        return new AccountOtpStartedResponse(
                challengeId,
                accountId,
                "OTP_SENT",
                OtpUtils.maskEmail(registeredEmail),
                "KYC verified. OTP sent to registered email. Account will be created only after OTP confirmation.");
    }

    @PostMapping("/onboarding/confirm")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public AccountResponse confirmOnboarding(
            @Valid @RequestBody ConfirmAccountOnboardingRequest request) {
        OtpChallenge challenge = challenges.findById(request.getChallengeId())
                .orElseThrow(() -> new IllegalArgumentException("OTP challenge not found"));

        validateOnboardingChallenge(challenge);
        if (!OtpUtils.isOtpValid(challenge, request.getOtp())) {
            challenge.incrementAttempts();
            challenges.save(challenge);
            throw new IllegalArgumentException("Invalid OTP");
        }

        GovernmentProof proof = governmentProofs.findById(challenge.getKycReference())
                .orElseThrow(() -> new IllegalStateException(
                        "Trusted KYC record linked to this OTP challenge was not found"));
        validateKyc(proof);

        if (customers.existsByKycReference(proof.getId())) {
            throw new IllegalArgumentException("A customer is already registered for this KYC record");
        }
        if (customers.existsByEmail(proof.getRegisteredEmail())) {
            throw new IllegalArgumentException("A customer is already registered with this email");
        }

        // Only fields actually present in the trusted KYC record are populated.
        // Mobile/address fields remain null until trusted source data is available.
        Customer customer = customers.save(new Customer(
                proof.getFirstName(),
                proof.getMiddleName(),
                proof.getLastName(),
                proof.getDateOfBirth(),
                proof.getRegisteredEmail(),
                null, null, null, null, null, null, null,
                proof.getId()));

        Account account = service.createAccount(
                challenge.getRequestedAccountId(),
                java.math.BigDecimal.ZERO,
                customer.getCustomerId());

        challenge.consume();
        challenges.save(challenge);
        return AccountResponse.from(account);
    }

    /** Returns account identifier, current balance, and linked customer profile. */
    @GetMapping("/{id}")
    public AccountDetailsResponse getAccount(@PathVariable String id) {
        Account account = service.getAccount(id);
        Customer customer = account.customerId() == null
                ? null
                : customers.findById(account.customerId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Customer profile linked to account was not found"));
        return AccountDetailsResponse.from(account, customer);
    }

    @GetMapping("/{id}/balance")
    public BalanceResponse getBalance(@PathVariable String id) {
        Account account = service.getAccount(id);
        return new BalanceResponse(account.id(), account.balance());
    }

    private void validateKyc(GovernmentProof proof) {
        if (!"VERIFIED".equalsIgnoreCase(proof.getValidationStatus())) {
            throw new IllegalArgumentException(
                    "KYC validation failed: government ID record is not verified");
        }
    }

    private void validateOnboardingChallenge(OtpChallenge challenge) {
        if (challenge.getPurpose() != OtpPurpose.CUSTOMER_ONBOARDING) {
            throw new IllegalArgumentException("OTP challenge is not for account onboarding");
        }
        OtpUtils.validateChallengeLifecycle(challenge, MAX_OTP_ATTEMPTS);
        if (challenge.getKycReference() == null
                || challenge.getRequestedAccountId() == null) {
            throw new IllegalStateException(
                    "This OTP challenge was created by an older version. Start onboarding again.");
        }
    }

}
