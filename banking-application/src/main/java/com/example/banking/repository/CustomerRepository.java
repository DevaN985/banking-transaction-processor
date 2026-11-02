package com.example.banking.repository;
import com.example.banking.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CustomerRepository extends JpaRepository<Customer,String> {
    boolean existsByEmail(String email);
    boolean existsByKycReference(String kycReference);
}
