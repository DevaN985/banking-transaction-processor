package com.example.banking.repository;
import com.example.banking.domain.GovernmentProof;
import com.example.banking.domain.enums.GovernmentIdType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface GovernmentProofRepository extends JpaRepository<GovernmentProof, String>  {
    Optional<GovernmentProof> findByGovernmentIdTypeAndGovernmentIdNumber(GovernmentIdType type, String number);
}
