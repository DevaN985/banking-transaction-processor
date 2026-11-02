package com.example.banking.service;

import com.example.banking.domain.Account;
import com.example.banking.domain.Customer;
import com.example.banking.domain.OtpChallenge;
import com.example.banking.domain.Transaction;
import com.example.banking.domain.TransactionType;
import com.example.banking.domain.enums.OtpPurpose;
import com.example.banking.dto.request.TransactionOtpConfirmRequest;
import com.example.banking.dto.request.TransactionOtpRequest;
import com.example.banking.dto.request.TransactionOtpResponse;
import com.example.banking.notification.EmailProvider;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.CustomerRepository;
import com.example.banking.repository.OtpChallengeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionAuthorizationServiceTest {

    private static final String ACCOUNT_ID = "ACC-100";
    private static final String DESTINATION_ACCOUNT_ID = "ACC-200";
    private static final String CUSTOMER_ID = "CUST-100";
    private static final String REGISTERED_EMAIL = "customer@example.com";
    private static final BigDecimal OTP_AMOUNT = new BigDecimal("1500.00");
    private static final String VALID_OTP = "123456";

    @Mock
    private OtpChallengeRepository challenges;

    @Mock
    private EmailProvider email;

    @Mock
    private TransactionService transactions;

    @Mock
    private AccountRepository accounts;

    @Mock
    private CustomerRepository customers;

    @InjectMocks
    private TransactionAuthorizationService service;

    private Account account;
    private Customer customer;

    @BeforeEach
    void setUp() {
        account = new Account(
                ACCOUNT_ID,
                new BigDecimal("5000.00"),
                CUSTOMER_ID
        );

        customer = new Customer(
                "Test",
                null,
                "Customer",
                LocalDate.of(1990, 1, 1),
                REGISTERED_EMAIL,
                "9876543210",
                "Address line 1",
                null,
                "Bengaluru",
                "Karnataka",
                "560001",
                "IN",
                "KYC-100"
        );
    }

    // -------------------------------------------------------------------------
    // requiresOtp()
    // -------------------------------------------------------------------------

    @Test
    void requiresOtpReturnsFalseForNullAmount() {
        assertFalse(service.requiresOtp(null));
    }

    @Test
    void requiresOtpReturnsFalseForAmountBelowThreshold() {
        assertFalse(service.requiresOtp(new BigDecimal("999.99")));
    }

    @Test
    void requiresOtpReturnsFalseForAmountEqualToThreshold() {
        assertFalse(service.requiresOtp(new BigDecimal("1000.00")));
    }

    @Test
    void requiresOtpReturnsTrueForAmountAboveThreshold() {
        assertTrue(service.requiresOtp(new BigDecimal("1000.01")));
    }

    // -------------------------------------------------------------------------
    // requestOtp() validation
    // -------------------------------------------------------------------------

    @Test
    void requestOtpRejectsAmountNotRequiringOtp() {
        TransactionOtpRequest request =
                request(TransactionOtpRequest.Operation.WITHDRAWAL,
                        ACCOUNT_ID, null, new BigDecimal("1000.00"));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.requestOtp(request)
        );

        verifyNoInteractions(accounts, customers, challenges, email);
    }

    @Test
    void requestOtpRejectsUnknownAccount() {
        TransactionOtpRequest request =
                request(TransactionOtpRequest.Operation.WITHDRAWAL,
                        ACCOUNT_ID, null, OTP_AMOUNT);

        when(accounts.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.requestOtp(request)
        );

        verifyNoInteractions(customers, challenges, email);
    }

    @Test
    void requestOtpRejectsAccountWithoutCustomerAssociation() {
        Account accountWithoutCustomer =
                new Account(ACCOUNT_ID, new BigDecimal("5000.00"));

        TransactionOtpRequest request =
                request(TransactionOtpRequest.Operation.WITHDRAWAL,
                        ACCOUNT_ID, null, OTP_AMOUNT);

        when(accounts.findById(ACCOUNT_ID))
                .thenReturn(Optional.of(accountWithoutCustomer));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.requestOtp(request)
        );

        verifyNoInteractions(customers, challenges, email);
    }

    @Test
    void requestOtpRejectsUnknownCustomer() {
        TransactionOtpRequest request =
                request(TransactionOtpRequest.Operation.WITHDRAWAL,
                        ACCOUNT_ID, null, OTP_AMOUNT);

        when(accounts.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(customers.findById(CUSTOMER_ID)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.requestOtp(request)
        );

        verifyNoInteractions(challenges, email);
    }

    @Test
    void requestOtpRejectsCustomerWithoutRegisteredEmail() {
        Customer customerWithoutEmail = new Customer(
                "Test",
                null,
                "Customer",
                LocalDate.of(1990, 1, 1),
                " ",
                "9876543210",
                "Address line 1",
                null,
                "Bengaluru",
                "Karnataka",
                "560001",
                "IN",
                "KYC-100"
        );

        TransactionOtpRequest request =
                request(TransactionOtpRequest.Operation.WITHDRAWAL,
                        ACCOUNT_ID, null, OTP_AMOUNT);

        when(accounts.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(customers.findById(CUSTOMER_ID))
                .thenReturn(Optional.of(customerWithoutEmail));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.requestOtp(request)
        );

        verifyNoInteractions(challenges, email);
    }

    @Test
    void requestOtpRejectsTransferWithoutDestination() {
        TransactionOtpRequest request =
                request(TransactionOtpRequest.Operation.TRANSFER,
                        ACCOUNT_ID, null, OTP_AMOUNT);

        stubRegisteredCustomer();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.requestOtp(request)
        );

        verifyNoInteractions(challenges, email);
    }

    @Test
    void requestOtpRejectsTransferToSameAccount() {
        TransactionOtpRequest request =
                request(TransactionOtpRequest.Operation.TRANSFER,
                        ACCOUNT_ID, ACCOUNT_ID, OTP_AMOUNT);

        stubRegisteredCustomer();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.requestOtp(request)
        );

        verify(accounts, never()).existsById(any());
        verifyNoInteractions(challenges, email);
    }

    @Test
    void requestOtpRejectsTransferToUnknownDestination() {
        TransactionOtpRequest request =
                request(TransactionOtpRequest.Operation.TRANSFER,
                        ACCOUNT_ID, DESTINATION_ACCOUNT_ID, OTP_AMOUNT);

        stubRegisteredCustomer();
        when(accounts.existsById(DESTINATION_ACCOUNT_ID)).thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.requestOtp(request)
        );

        verifyNoInteractions(challenges, email);
    }

    @Test
    void requestOtpRejectsWithdrawalWithDestinationAccount() {
        TransactionOtpRequest request =
                request(TransactionOtpRequest.Operation.WITHDRAWAL,
                        ACCOUNT_ID, DESTINATION_ACCOUNT_ID, OTP_AMOUNT);

        stubRegisteredCustomer();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.requestOtp(request)
        );

        verifyNoInteractions(challenges, email);
    }

    // -------------------------------------------------------------------------
    // requestOtp() successful flows
    // -------------------------------------------------------------------------

    @Test
    void requestOtpForWithdrawalPersistsChallengeAndEmailsRegisteredAddress() {
        TransactionOtpRequest request =
                request(TransactionOtpRequest.Operation.WITHDRAWAL,
                        ACCOUNT_ID, null, OTP_AMOUNT);

        stubRegisteredCustomer();

        when(challenges.save(any(OtpChallenge.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionOtpResponse response = service.requestOtp(request);

        assertNotNull(response.getChallengeId());
        assertEquals(ACCOUNT_ID, response.getAccountId());
        assertEquals("WITHDRAWAL", response.getOperation());
        assertEquals(OTP_AMOUNT, response.getAmount());
        assertEquals("OTP_SENT", response.getStatus());

        ArgumentCaptor<OtpChallenge> challengeCaptor =
                ArgumentCaptor.forClass(OtpChallenge.class);

        verify(challenges).save(challengeCaptor.capture());

        OtpChallenge savedChallenge = challengeCaptor.getValue();

        assertEquals(response.getChallengeId(), savedChallenge.getId());
        assertEquals(response.getChallengeId(), savedChallenge.getReferenceId());
        assertEquals(REGISTERED_EMAIL, savedChallenge.getEmail());
        assertEquals(OtpPurpose.WITHDRAWAL_AUTHORIZATION, savedChallenge.getPurpose());
        assertEquals(ACCOUNT_ID, savedChallenge.getTransactionAccountId());
        assertNull(savedChallenge.getDestinationAccountId());
        assertEquals("WITHDRAWAL", savedChallenge.getTransactionOperation());
        assertEquals(OTP_AMOUNT, savedChallenge.getTransactionAmount());
        assertNotNull(savedChallenge.getOtpHash());
        assertEquals(64, savedChallenge.getOtpHash().length());
        assertTrue(savedChallenge.getExpiresAt().isAfter(Instant.now()));

        ArgumentCaptor<String> emailCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(email).sendEmail(
                eq(REGISTERED_EMAIL),
                eq("Banking transaction confirmation OTP"),
                emailCaptor.capture()
        );

        String emailBody = emailCaptor.getValue();
        assertTrue(emailBody.contains("Your OTP is "));
        assertTrue(emailBody.contains("expires in 5 minutes"));
        assertTrue(emailBody.contains("WITHDRAWAL"));
        assertTrue(emailBody.contains("1500.00"));
    }

    @Test
    void requestOtpForTransferPersistsTransferChallenge() {
        TransactionOtpRequest request =
                request(TransactionOtpRequest.Operation.TRANSFER,
                        ACCOUNT_ID, DESTINATION_ACCOUNT_ID, OTP_AMOUNT);

        stubRegisteredCustomer();
        when(accounts.existsById(DESTINATION_ACCOUNT_ID)).thenReturn(true);
        when(challenges.save(any(OtpChallenge.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionOtpResponse response = service.requestOtp(request);

        assertNotNull(response.getChallengeId());
        assertEquals("TRANSFER", response.getOperation());

        ArgumentCaptor<OtpChallenge> captor =
                ArgumentCaptor.forClass(OtpChallenge.class);

        verify(challenges).save(captor.capture());

        OtpChallenge savedChallenge = captor.getValue();

        assertEquals(OtpPurpose.TRANSFER_AUTHORIZATION, savedChallenge.getPurpose());
        assertEquals(ACCOUNT_ID, savedChallenge.getTransactionAccountId());
        assertEquals(DESTINATION_ACCOUNT_ID, savedChallenge.getDestinationAccountId());
        assertEquals("TRANSFER", savedChallenge.getTransactionOperation());
        assertEquals(OTP_AMOUNT, savedChallenge.getTransactionAmount());
    }

    // -------------------------------------------------------------------------
    // confirm() validation
    // -------------------------------------------------------------------------

    @Test
    void confirmRejectsUnknownChallenge() {
        TransactionOtpConfirmRequest request =
                confirmRequest("missing-challenge", VALID_OTP);

        when(challenges.findById("missing-challenge"))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.confirm(request)
        );

        verifyNoInteractions(transactions);
    }

    @Test
    void confirmRejectsMismatchedReference() {
        OtpChallenge challenge = challenge(
                "challenge-id",
                "different-reference",
                OtpPurpose.WITHDRAWAL_AUTHORIZATION,
                "WITHDRAWAL",
                ACCOUNT_ID,
                null,
                OTP_AMOUNT,
                VALID_OTP,
                Instant.now().plusSeconds(300)
        );

        when(challenges.findById("challenge-id"))
                .thenReturn(Optional.of(challenge));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.confirm(confirmRequest("challenge-id", VALID_OTP))
        );

        verifyNoInteractions(transactions);
    }

    @Test
    void confirmRejectsChallengeMissingTransactionDetails() {
        OtpChallenge challenge = new OtpChallenge(
                "challenge-id",
                "challenge-id",
                REGISTERED_EMAIL,
                OtpPurpose.WITHDRAWAL_AUTHORIZATION,
                hash(VALID_OTP),
                Instant.now().plusSeconds(300)
        );

        when(challenges.findById("challenge-id"))
                .thenReturn(Optional.of(challenge));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.confirm(confirmRequest("challenge-id", VALID_OTP))
        );

        verifyNoInteractions(transactions);
    }

    @Test
    void confirmRejectsExpiredChallenge() {
        OtpChallenge challenge = challenge(
                "challenge-id",
                "challenge-id",
                OtpPurpose.WITHDRAWAL_AUTHORIZATION,
                "WITHDRAWAL",
                ACCOUNT_ID,
                null,
                OTP_AMOUNT,
                VALID_OTP,
                Instant.now().minusSeconds(10)
        );

        when(challenges.findById("challenge-id"))
                .thenReturn(Optional.of(challenge));

        assertThrows(
                IllegalStateException.class,
                () -> service.confirm(confirmRequest("challenge-id", VALID_OTP))
        );

        verifyNoInteractions(transactions);
    }

    @Test
    void confirmRejectsConsumedChallenge() {
        OtpChallenge challenge = challenge(
                "challenge-id",
                "challenge-id",
                OtpPurpose.WITHDRAWAL_AUTHORIZATION,
                "WITHDRAWAL",
                ACCOUNT_ID,
                null,
                OTP_AMOUNT,
                VALID_OTP,
                Instant.now().plusSeconds(300)
        );
        challenge.consume();

        when(challenges.findById("challenge-id"))
                .thenReturn(Optional.of(challenge));

        assertThrows(
                IllegalStateException.class,
                () -> service.confirm(confirmRequest("challenge-id", VALID_OTP))
        );

        verifyNoInteractions(transactions);
    }

    @Test
    void confirmRejectsChallengeAfterMaximumAttempts() {
        OtpChallenge challenge = challenge(
                "challenge-id",
                "challenge-id",
                OtpPurpose.WITHDRAWAL_AUTHORIZATION,
                "WITHDRAWAL",
                ACCOUNT_ID,
                null,
                OTP_AMOUNT,
                VALID_OTP,
                Instant.now().plusSeconds(300)
        );

        for (int i = 0; i < 5; i++) {
            challenge.incrementAttempts();
        }

        when(challenges.findById("challenge-id"))
                .thenReturn(Optional.of(challenge));

        assertThrows(
                IllegalStateException.class,
                () -> service.confirm(confirmRequest("challenge-id", VALID_OTP))
        );

        verifyNoInteractions(transactions);
    }

    @Test
    void confirmRejectsInvalidOtpAndIncrementsAttempts() {
        OtpChallenge challenge = challenge(
                "challenge-id",
                "challenge-id",
                OtpPurpose.WITHDRAWAL_AUTHORIZATION,
                "WITHDRAWAL",
                ACCOUNT_ID,
                null,
                OTP_AMOUNT,
                VALID_OTP,
                Instant.now().plusSeconds(300)
        );

        when(challenges.findById("challenge-id"))
                .thenReturn(Optional.of(challenge));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.confirm(confirmRequest("challenge-id", "000000"))
        );

        assertEquals(1, challenge.getAttempts());
        verify(challenges).save(challenge);
        verifyNoInteractions(transactions);
    }

    @Test
    void confirmRejectsTransferWithWrongPurpose() {
        OtpChallenge challenge = challenge(
                "challenge-id",
                "challenge-id",
                OtpPurpose.WITHDRAWAL_AUTHORIZATION,
                "TRANSFER",
                ACCOUNT_ID,
                DESTINATION_ACCOUNT_ID,
                OTP_AMOUNT,
                VALID_OTP,
                Instant.now().plusSeconds(300)
        );

        when(challenges.findById("challenge-id"))
                .thenReturn(Optional.of(challenge));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.confirm(confirmRequest("challenge-id", VALID_OTP))
        );

        verifyNoInteractions(transactions);
    }

    @Test
    void confirmRejectsTransferWithoutDestination() {
        OtpChallenge challenge = challenge(
                "challenge-id",
                "challenge-id",
                OtpPurpose.TRANSFER_AUTHORIZATION,
                "TRANSFER",
                ACCOUNT_ID,
                null,
                OTP_AMOUNT,
                VALID_OTP,
                Instant.now().plusSeconds(300)
        );

        when(challenges.findById("challenge-id"))
                .thenReturn(Optional.of(challenge));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.confirm(confirmRequest("challenge-id", VALID_OTP))
        );

        verifyNoInteractions(transactions);
    }

    @Test
    void confirmRejectsWithdrawalWithWrongPurpose() {
        OtpChallenge challenge = challenge(
                "challenge-id",
                "challenge-id",
                OtpPurpose.TRANSFER_AUTHORIZATION,
                "WITHDRAWAL",
                ACCOUNT_ID,
                null,
                OTP_AMOUNT,
                VALID_OTP,
                Instant.now().plusSeconds(300)
        );

        when(challenges.findById("challenge-id"))
                .thenReturn(Optional.of(challenge));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.confirm(confirmRequest("challenge-id", VALID_OTP))
        );

        verifyNoInteractions(transactions);
    }

    // -------------------------------------------------------------------------
    // confirm() successful flows
    // -------------------------------------------------------------------------

    @Test
    void confirmSuccessfulWithdrawalExecutesTransactionAndConsumesChallenge() {
        OtpChallenge challenge = challenge(
                "challenge-id",
                "challenge-id",
                OtpPurpose.WITHDRAWAL_AUTHORIZATION,
                "WITHDRAWAL",
                ACCOUNT_ID,
                null,
                OTP_AMOUNT,
                VALID_OTP,
                Instant.now().plusSeconds(300)
        );

        Transaction transaction = new Transaction(
                "TX-100",
                ACCOUNT_ID,
                TransactionType.WITHDRAWAL,
                OTP_AMOUNT,
                Instant.now(),
                null
        );

        when(challenges.findById("challenge-id"))
                .thenReturn(Optional.of(challenge));

        when(transactions.withdraw(ACCOUNT_ID, OTP_AMOUNT))
                .thenReturn(transaction);

        String result =
                service.confirm(confirmRequest("challenge-id", VALID_OTP));

        assertEquals("TX-100", result);
        assertTrue(challenge.isConsumed());

        verify(transactions).withdraw(ACCOUNT_ID, OTP_AMOUNT);
        verify(challenges).save(challenge);
    }

    @Test
    void confirmSuccessfulTransferExecutesTransferAndConsumesChallenge() {
        OtpChallenge challenge = challenge(
                "challenge-id",
                "challenge-id",
                OtpPurpose.TRANSFER_AUTHORIZATION,
                "TRANSFER",
                ACCOUNT_ID,
                DESTINATION_ACCOUNT_ID,
                OTP_AMOUNT,
                VALID_OTP,
                Instant.now().plusSeconds(300)
        );

        String transferReference = UUID.randomUUID().toString();

        when(challenges.findById("challenge-id"))
                .thenReturn(Optional.of(challenge));

        when(transactions.transfer(
                ACCOUNT_ID,
                DESTINATION_ACCOUNT_ID,
                OTP_AMOUNT
        )).thenReturn(transferReference);

        String result =
                service.confirm(confirmRequest("challenge-id", VALID_OTP));

        assertEquals(transferReference, result);
        assertTrue(challenge.isConsumed());

        verify(transactions).transfer(
                ACCOUNT_ID,
                DESTINATION_ACCOUNT_ID,
                OTP_AMOUNT
        );
        verify(challenges).save(challenge);
    }

    // -------------------------------------------------------------------------
    // Test helpers
    // -------------------------------------------------------------------------

    private void stubRegisteredCustomer() {
        when(accounts.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(customers.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer));
    }

    private TransactionOtpRequest request(
            TransactionOtpRequest.Operation operation,
            String accountId,
            String destinationAccountId,
            BigDecimal amount
    ) {
        TransactionOtpRequest request = new TransactionOtpRequest();
        request.setOperation(operation);
        request.setAccountId(accountId);
        request.setDestinationAccountId(destinationAccountId);
        request.setAmount(amount);
        return request;
    }

    private TransactionOtpConfirmRequest confirmRequest(
            String challengeId,
            String otp
    ) {
        TransactionOtpConfirmRequest request =
                new TransactionOtpConfirmRequest();
        request.setChallengeId(challengeId);
        request.setOtp(otp);
        return request;
    }

    private OtpChallenge challenge(
            String id,
            String referenceId,
            OtpPurpose purpose,
            String operation,
            String accountId,
            String destinationAccountId,
            BigDecimal amount,
            String otp,
            Instant expiresAt
    ) {
        return new OtpChallenge(
                id,
                referenceId,
                REGISTERED_EMAIL,
                purpose,
                hash(otp),
                expiresAt,
                accountId,
                destinationAccountId,
                operation,
                amount
        );
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256")
                            .digest(value.getBytes(StandardCharsets.UTF_8))
            );
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}