package com.example.banking.domain;

import com.example.banking.domain.enums.OtpPurpose;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class OtpChallengeTest {
    @Test void initializesLifecycleAndOnboardingFields() {
        OtpChallenge c = new OtpChallenge("id","ref","x@y.com",OtpPurpose.CUSTOMER_ONBOARDING,"hash",Instant.now().plusSeconds(60),"kyc","acc",BigDecimal.ZERO);
        assertEquals(0,c.getAttempts()); assertFalse(c.isConsumed()); assertEquals("kyc",c.getKycReference());
        assertEquals("acc",c.getRequestedAccountId()); assertEquals(BigDecimal.ZERO,c.getOpeningBalance());
        c.incrementAttempts(); c.consume(); assertEquals(1,c.getAttempts()); assertTrue(c.isConsumed());
    }
    @Test void initializesTransactionChallengeFieldsAndAccessors() {
        OtpChallenge c = new OtpChallenge("id","ref","x@y.com",OtpPurpose.TRANSFER_AUTHORIZATION,"hash",Instant.now().plusSeconds(60),"A1","A2","TRANSFER",new BigDecimal("25"));
        assertEquals("A1",c.getTransactionAccountId()); assertEquals("A2",c.getDestinationAccountId());
        assertEquals("TRANSFER",c.getTransactionOperation()); assertEquals(new BigDecimal("25"),c.getTransactionAmount());
        assertEquals("id",c.getId()); assertEquals("ref",c.getReferenceId()); assertEquals("x@y.com",c.getEmail());
        assertEquals(OtpPurpose.TRANSFER_AUTHORIZATION,c.getPurpose()); assertEquals("hash",c.getOtpHash()); assertNotNull(c.getExpiresAt());
    }
}
