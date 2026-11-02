package com.example.banking.repository;

import com.example.banking.domain.OtpChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOtpChallengeRepository
        extends JpaRepository<OtpChallenge, String>,
                com.example.banking.repository.OtpChallengeRepository {
}
