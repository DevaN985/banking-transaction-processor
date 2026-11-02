package com.example.banking.repository;
import com.example.banking.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String>  {
    List<Transaction> findByAccountIdOrderByTimestampAsc(String accountId);
}
