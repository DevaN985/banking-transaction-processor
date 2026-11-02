-- Step 5: one-time-password challenges for onboarding and transaction authorization.
CREATE TABLE otp_challenges (
    id VARCHAR(36) NOT NULL,
    reference_id VARCHAR(36) NOT NULL,
    email VARCHAR(254) NOT NULL,
    purpose VARCHAR(40) NOT NULL,
    otp_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    attempts INTEGER NOT NULL,
    consumed INTEGER NOT NULL,
    CONSTRAINT pk_otp_challenges PRIMARY KEY (id),
    CONSTRAINT uk_otp_challenges_reference UNIQUE (reference_id),
    CONSTRAINT ck_otp_challenges_consumed CHECK (consumed IN (0, 1))
);
