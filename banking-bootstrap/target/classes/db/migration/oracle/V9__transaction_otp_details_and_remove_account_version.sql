-- Remove optimistic-lock version column from accounts and persist transaction details in OTP challenges.
ALTER TABLE accounts DROP COLUMN version;
ALTER TABLE otp_challenges ADD (
    transaction_account_id VARCHAR2(100 CHAR),
    destination_account_id VARCHAR2(100 CHAR),
    transaction_operation VARCHAR2(20 CHAR),
    transaction_amount NUMBER(19,4)
);
