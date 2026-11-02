package com.example.banking.util;

import com.example.banking.domain.OtpChallenge;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;

/** Reusable OTP generation, hashing, validation, and email masking helpers. */
public final class OtpUtils {
    private static final int DEFAULT_OTP_LENGTH = 6;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private OtpUtils() {
    }

    public static String generateOtp() {
        return String.format("%0" + DEFAULT_OTP_LENGTH + "d",
                SECURE_RANDOM.nextInt(1_000_000));
    }

    public static String hash(String value) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256")
                            .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to hash OTP", exception);
        }
    }

    public static boolean isOtpValid(OtpChallenge challenge, String suppliedOtp) {
        if (suppliedOtp == null || suppliedOtp.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(
                challenge.getOtpHash().getBytes(StandardCharsets.UTF_8),
                hash(suppliedOtp).getBytes(StandardCharsets.UTF_8));
    }

    /** Validates common OTP lifecycle rules; purpose-specific checks stay in the calling service. */
    public static void validateChallengeLifecycle(OtpChallenge challenge, int maxAttempts) {
        if (challenge.isConsumed() || Instant.now().isAfter(challenge.getExpiresAt())) {
            throw new IllegalStateException("OTP expired or already used");
        }
        if (challenge.getAttempts() >= maxAttempts) {
            throw new IllegalStateException("Maximum OTP attempts exceeded");
        }
    }

    public static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "***";
        }
        int atIndex = email.indexOf('@');
        if (atIndex < 1) {
            return "***";
        }
        return email.substring(0, 1) + "***" + email.substring(atIndex);
    }
}
