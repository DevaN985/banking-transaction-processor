package com.example.banking.repository;
import com.example.banking.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AccountRepository extends JpaRepository<Account, String>  {
}
