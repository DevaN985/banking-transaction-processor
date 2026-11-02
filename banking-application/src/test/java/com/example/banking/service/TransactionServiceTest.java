package com.example.banking.service;

import com.example.banking.domain.Account;
import com.example.banking.domain.Transaction;
import com.example.banking.domain.TransactionType;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.TransactionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private static final String ACCOUNT_ID = "ACC-100";
    private static final String CUSTOMER_ID = "CUST-100";
    private static final String DESTINATION_ACCOUNT_ID = "ACC-200";

    @Mock
    private AccountRepository accounts;

    @Mock
    private TransactionRepository transactions;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private TransactionService service;

    @Test
    void accountExistsReturnsTrueWhenAccountExists() {
        when(accounts.existsById(ACCOUNT_ID)).thenReturn(true);

        assertTrue(service.accountExists(ACCOUNT_ID));

        verify(accounts).existsById(ACCOUNT_ID);
    }

    @Test
    void accountExistsReturnsFalseWhenAccountDoesNotExist() {
        when(accounts.existsById(ACCOUNT_ID)).thenReturn(false);

        assertFalse(service.accountExists(ACCOUNT_ID));

        verify(accounts).existsById(ACCOUNT_ID);
    }

    @Test
    void createAccountSavesAccountWhenIdIsAvailable() {
        BigDecimal openingBalance = new BigDecimal("100.00");

        when(accounts.existsById(ACCOUNT_ID)).thenReturn(false);
        when(accounts.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Account result = service.createAccount(
                ACCOUNT_ID,
                openingBalance,
                CUSTOMER_ID
        );

        assertNotNull(result);
        assertEquals(ACCOUNT_ID, result.id());
        assertEquals(openingBalance, result.balance());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accounts).save(captor.capture());

        assertEquals(ACCOUNT_ID, captor.getValue().id());
        assertEquals(CUSTOMER_ID, captor.getValue().customerId());
    }

    @Test
    void createAccountThrowsWhenAccountAlreadyExists() {
        when(accounts.existsById(ACCOUNT_ID)).thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.createAccount(
                        ACCOUNT_ID,
                        new BigDecimal("100.00"),
                        CUSTOMER_ID
                )
        );

        verify(accounts, never()).save(any(Account.class));
    }

    @Test
    void getAccountReturnsAccountWhenFound() {
        Account account = new Account(
                ACCOUNT_ID,
                new BigDecimal("500.00"),
                CUSTOMER_ID
        );

        when(accounts.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));

        Account result = service.getAccount(ACCOUNT_ID);

        assertSame(account, result);
    }

    @Test
    void getAccountThrowsWhenAccountDoesNotExist() {
        when(accounts.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

        assertThrows(
                TransactionService.AccountNotFoundException.class,
                () -> service.getAccount(ACCOUNT_ID)
        );
    }

    @Test
    void historyReturnsTransactionsForAccount() {
        Account account = new Account(
                ACCOUNT_ID,
                new BigDecimal("500.00"),
                CUSTOMER_ID
        );

        Transaction transaction = mock(Transaction.class);
        List<Transaction> history = List.of(transaction);

        when(accounts.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(transactions.findByAccountIdOrderByTimestampAsc(ACCOUNT_ID))
                .thenReturn(history);

        assertEquals(history, service.history(ACCOUNT_ID));

        verify(transactions)
                .findByAccountIdOrderByTimestampAsc(ACCOUNT_ID);
    }

    @Test
    void historyThrowsWhenAccountDoesNotExist() {
        when(accounts.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

        assertThrows(
                TransactionService.AccountNotFoundException.class,
                () -> service.history(ACCOUNT_ID)
        );

        verify(transactions, never())
                .findByAccountIdOrderByTimestampAsc(anyString());
    }

    @Test
    void depositIncreasesBalanceAndSavesDepositTransaction() {
        Account account = new Account(
                ACCOUNT_ID,
                new BigDecimal("100.00"),
                CUSTOMER_ID
        );

        when(entityManager.find(
                eq(Account.class),
                eq(ACCOUNT_ID),
                eq(LockModeType.PESSIMISTIC_WRITE)
        )).thenReturn(account);

        when(transactions.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = service.deposit(
                ACCOUNT_ID,
                new BigDecimal("10.50")
        );

        assertEquals(new BigDecimal("110.50"), account.balance());
        assertEquals(TransactionType.DEPOSIT, result.type());
        assertEquals(new BigDecimal("10.50"), result.amount());
        assertEquals(ACCOUNT_ID, result.accountId());

        verify(transactions).save(any(Transaction.class));
    }

    @Test
    void depositThrowsWhenAccountDoesNotExist() {
        when(entityManager.find(
                eq(Account.class),
                eq(ACCOUNT_ID),
                eq(LockModeType.PESSIMISTIC_WRITE)
        )).thenReturn(null);

        assertThrows(
                TransactionService.AccountNotFoundException.class,
                () -> service.deposit(
                        ACCOUNT_ID,
                        new BigDecimal("10.00")
                )
        );

        verify(transactions, never()).save(any(Transaction.class));
    }

    @Test
    void withdrawReducesBalanceAndSavesWithdrawalTransaction() {
        Account account = new Account(
                ACCOUNT_ID,
                new BigDecimal("100.00"),
                CUSTOMER_ID
        );

        when(entityManager.find(
                eq(Account.class),
                eq(ACCOUNT_ID),
                eq(LockModeType.PESSIMISTIC_WRITE)
        )).thenReturn(account);

        when(transactions.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = service.withdraw(
                ACCOUNT_ID,
                new BigDecimal("25.00")
        );

        assertEquals(new BigDecimal("75.00"), account.balance());
        assertEquals(TransactionType.WITHDRAWAL, result.type());
        assertEquals(new BigDecimal("25.00"), result.amount());

        verify(transactions).save(any(Transaction.class));
    }

    @Test
    void withdrawThrowsWhenAccountDoesNotExist() {
        when(entityManager.find(
                eq(Account.class),
                eq(ACCOUNT_ID),
                eq(LockModeType.PESSIMISTIC_WRITE)
        )).thenReturn(null);

        assertThrows(
                TransactionService.AccountNotFoundException.class,
                () -> service.withdraw(
                        ACCOUNT_ID,
                        new BigDecimal("10.00")
                )
        );

        verify(transactions, never()).save(any(Transaction.class));
    }

    @Test
    void transferMovesMoneyAndSavesTwoLinkedTransactions() {
        Account source = new Account(
                ACCOUNT_ID,
                new BigDecimal("100.00"),
                CUSTOMER_ID
        );

        Account destination = new Account(
                DESTINATION_ACCOUNT_ID,
                new BigDecimal("25.00"),
                "CUST-200"
        );

        // ACC-100 sorts before ACC-200, so source is locked first.
        when(entityManager.find(
                eq(Account.class),
                eq(ACCOUNT_ID),
                eq(LockModeType.PESSIMISTIC_WRITE)
        )).thenReturn(source);

        when(entityManager.find(
                eq(Account.class),
                eq(DESTINATION_ACCOUNT_ID),
                eq(LockModeType.PESSIMISTIC_WRITE)
        )).thenReturn(destination);

        String transferId = service.transfer(
                ACCOUNT_ID,
                DESTINATION_ACCOUNT_ID,
                new BigDecimal("30.00")
        );

        assertNotNull(transferId);
        assertEquals(new BigDecimal("70.00"), source.balance());
        assertEquals(new BigDecimal("55.00"), destination.balance());

        ArgumentCaptor<Transaction> captor =
                ArgumentCaptor.forClass(Transaction.class);

        verify(transactions, times(2)).save(captor.capture());

        List<Transaction> saved = captor.getAllValues();

        assertEquals(TransactionType.TRANSFER_OUT, saved.get(0).type());
        assertEquals(TransactionType.TRANSFER_IN, saved.get(1).type());

        assertEquals(transferId, saved.get(0).transferId());
        assertEquals(transferId, saved.get(1).transferId());

        assertEquals(ACCOUNT_ID, saved.get(0).accountId());
        assertEquals(DESTINATION_ACCOUNT_ID, saved.get(1).accountId());
    }

    @Test
    void transferThrowsWhenSourceAndDestinationAreSame() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.transfer(
                        ACCOUNT_ID,
                        ACCOUNT_ID,
                        new BigDecimal("10.00")
                )
        );

        verifyNoInteractions(entityManager, transactions);
    }

    @Test
    void transferThrowsWhenAccountIdIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.transfer(
                        null,
                        DESTINATION_ACCOUNT_ID,
                        new BigDecimal("10.00")
                )
        );

        verifyNoInteractions(entityManager, transactions);
    }

    @Test
    void transferThrowsWhenAmountIsZero() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.transfer(
                        ACCOUNT_ID,
                        DESTINATION_ACCOUNT_ID,
                        BigDecimal.ZERO
                )
        );

        verifyNoInteractions(entityManager, transactions);
    }

    @Test
    void transferThrowsWhenAmountIsNegative() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.transfer(
                        ACCOUNT_ID,
                        DESTINATION_ACCOUNT_ID,
                        new BigDecimal("-10.00")
                )
        );

        verifyNoInteractions(entityManager, transactions);
    }

    @Test
    void transferThrowsWhenSourceAccountHasInsufficientFunds() {
        Account source = new Account(
                ACCOUNT_ID,
                new BigDecimal("20.00"),
                CUSTOMER_ID
        );

        Account destination = new Account(
                DESTINATION_ACCOUNT_ID,
                new BigDecimal("25.00"),
                "CUST-200"
        );

        when(entityManager.find(
                eq(Account.class),
                eq(ACCOUNT_ID),
                eq(LockModeType.PESSIMISTIC_WRITE)
        )).thenReturn(source);

        when(entityManager.find(
                eq(Account.class),
                eq(DESTINATION_ACCOUNT_ID),
                eq(LockModeType.PESSIMISTIC_WRITE)
        )).thenReturn(destination);

        assertThrows(
                IllegalStateException.class,
                () -> service.transfer(
                        ACCOUNT_ID,
                        DESTINATION_ACCOUNT_ID,
                        new BigDecimal("30.00")
                )
        );

        assertEquals(new BigDecimal("20.00"), source.balance());
        assertEquals(new BigDecimal("25.00"), destination.balance());

        verify(transactions, never()).save(any(Transaction.class));
    }

    @Test
    void transferThrowsWhenSourceAccountDoesNotExist() {
        when(entityManager.find(
                eq(Account.class),
                eq(ACCOUNT_ID),
                eq(LockModeType.PESSIMISTIC_WRITE)
        )).thenReturn(null);

        assertThrows(
                TransactionService.AccountNotFoundException.class,
                () -> service.transfer(
                        ACCOUNT_ID,
                        DESTINATION_ACCOUNT_ID,
                        new BigDecimal("10.00")
                )
        );

        verify(transactions, never()).save(any(Transaction.class));
    }
}