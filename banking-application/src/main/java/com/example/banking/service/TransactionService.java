package com.example.banking.service;

import com.example.banking.domain.Account;
import com.example.banking.domain.Transaction;
import com.example.banking.domain.TransactionType;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.TransactionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {
    private final AccountRepository accounts;
    private final TransactionRepository transactions;
    private final EntityManager entityManager;

    public TransactionService(AccountRepository accounts, TransactionRepository transactions,
                              EntityManager entityManager) {
        this.accounts = accounts;
        this.transactions = transactions;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public boolean accountExists(String id) {
        return accounts.existsById(id);
    }

    @Transactional
    public Account createAccount(String id, BigDecimal openingBalance, String customerId) {
        if (accounts.existsById(id)) throw new IllegalArgumentException("Account already exists: " + id);
        return accounts.save(new Account(id, openingBalance, customerId));
    }

    @Transactional(readOnly = true)
    public Account getAccount(String id) {
        return accounts.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Transaction> history(String id) {
        getAccount(id);
        return transactions.findByAccountIdOrderByTimestampAsc(id);
    }

    @Transactional
    public Transaction deposit(String id, BigDecimal amount) {
        Account account = findForUpdate(id);
        account.deposit(amount);
        return transactions.save(new Transaction(UUID.randomUUID().toString(), id, TransactionType.DEPOSIT,
                amount, Instant.now(), null));
    }

    @Transactional
    public Transaction withdraw(String id, BigDecimal amount) {
        Account account = findForUpdate(id);
        account.withdraw(amount);
        return transactions.save(new Transaction(UUID.randomUUID().toString(), id, TransactionType.WITHDRAWAL,
                amount, Instant.now(), null));
    }

    @Transactional
    public String transfer(String fromId, String toId, BigDecimal amount) {
        if (fromId == null || toId == null || fromId.equals(toId))
            throw new IllegalArgumentException("Source and destination must be different accounts");
        validatePositive(amount);
        // Lock accounts in stable ID order to reduce deadlock risk for opposite-direction transfers.
        Account first = findForUpdate(fromId.compareTo(toId) < 0 ? fromId : toId);
        Account second = findForUpdate(fromId.compareTo(toId) < 0 ? toId : fromId);
        Account from = fromId.equals(first.id()) ? first : second;
        Account to = toId.equals(first.id()) ? first : second;
        if (from.balance().compareTo(amount) < 0) throw new IllegalStateException("Insufficient funds");
        String transferId = UUID.randomUUID().toString();
        from.withdraw(amount);
        to.deposit(amount);
        Instant now = Instant.now();
        transactions.save(new Transaction(UUID.randomUUID().toString(), fromId, TransactionType.TRANSFER_OUT, amount, now, transferId));
        transactions.save(new Transaction(UUID.randomUUID().toString(), toId, TransactionType.TRANSFER_IN, amount, now, transferId));
        return transferId;
    }

    private Account findForUpdate(String id) {
        Account account = entityManager.find(Account.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (account == null) throw new AccountNotFoundException(id);
        return account;
    }

    private static void validatePositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0)
            throw new IllegalArgumentException("Amount must be greater than zero");
    }

    public static class AccountNotFoundException extends RuntimeException {
        public AccountNotFoundException(String id) {
            super("Account not found: " + id);
        }
    }
}
