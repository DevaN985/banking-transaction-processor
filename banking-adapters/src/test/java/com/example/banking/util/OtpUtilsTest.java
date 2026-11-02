package com.example.banking.util;

import com.example.banking.domain.OtpChallenge;
import com.example.banking.domain.enums.OtpPurpose;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class OtpUtilsTest {
    private OtpChallenge challenge(String otp, Instant expiry) { return new OtpChallenge("id","ref","user@example.com",OtpPurpose.WITHDRAWAL_AUTHORIZATION,OtpUtils.hash(otp),expiry); }
    @Test void generatedOtpIsSixDigitsIncludingLeadingZeroPossibility() { assertTrue(OtpUtils.generateOtp().matches("\\d{6}")); }
    @Test void hashIsStableAndOtpComparisonWorks() { assertEquals(OtpUtils.hash("123456"),OtpUtils.hash("123456")); assertNotEquals(OtpUtils.hash("123456"),OtpUtils.hash("654321")); assertTrue(OtpUtils.isOtpValid(challenge("123456",Instant.now().plusSeconds(60)),"123456")); assertFalse(OtpUtils.isOtpValid(challenge("123456",Instant.now().plusSeconds(60)),"654321")); assertFalse(OtpUtils.isOtpValid(challenge("123456",Instant.now().plusSeconds(60))," ")); assertFalse(OtpUtils.isOtpValid(challenge("123456",Instant.now().plusSeconds(60)),null)); }
    @Test void validatesConsumedExpiredAndAttemptLimit() {
        assertDoesNotThrow(() -> OtpUtils.validateChallengeLifecycle(challenge("123456",Instant.now().plusSeconds(60)),5));
        OtpChallenge consumed=challenge("123456",Instant.now().plusSeconds(60)); consumed.consume(); assertThrows(IllegalStateException.class,()->OtpUtils.validateChallengeLifecycle(consumed,5));
        assertThrows(IllegalStateException.class,()->OtpUtils.validateChallengeLifecycle(challenge("123456",Instant.now().minusSeconds(60)),5));
        OtpChallenge attempts=challenge("123456",Instant.now().plusSeconds(60)); for(int i=0;i<5;i++) attempts.incrementAttempts(); assertThrows(IllegalStateException.class,()->OtpUtils.validateChallengeLifecycle(attempts,5));
    }
    @Test void masksEmailAndHandlesInvalidInput() { assertEquals("u***@example.com",OtpUtils.maskEmail("user@example.com")); assertEquals("***",OtpUtils.maskEmail(null)); assertEquals("***",OtpUtils.maskEmail(" ")); assertEquals("***",OtpUtils.maskEmail("invalid")); }
}
