package com.example.banking.repository;

import com.example.banking.domain.OtpChallenge;

import java.util.Optional;

/** Application port for persisting and retrieving OTP challenges. */
public interface OtpChallengeRepository {

    OtpChallenge save(OtpChallenge challenge);

    Optional<OtpChallenge> findById(String id);
}
