-- Step 5: one-time-password challenges for onboarding and transaction authorization.
CREATE TABLE otp_challenges (
    id VARCHAR2(36 CHAR) NOT NULL,
    reference_id VARCHAR2(36 CHAR) NOT NULL,
    email VARCHAR2(254 CHAR) NOT NULL,
    purpose VARCHAR2(40 CHAR) NOT NULL,
    otp_hash VARCHAR2(64 CHAR) NOT NULL,
    expires_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    attempts NUMBER(10,0) NOT NULL,
    consumed NUMBER(1,0) NOT NULL,
    CONSTRAINT pk_otp_challenges PRIMARY KEY (id),
    CONSTRAINT uk_otp_challenges_reference UNIQUE (reference_id),
    CONSTRAINT ck_otp_challenges_consumed CHECK (consumed IN (0, 1))
);
