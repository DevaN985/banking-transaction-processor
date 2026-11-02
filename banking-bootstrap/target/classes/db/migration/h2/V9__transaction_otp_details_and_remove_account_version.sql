-- Remove optimistic-lock version column from accounts and persist transaction details in OTP challenges.
ALTER TABLE accounts DROP COLUMN version;
ALTER TABLE otp_challenges ADD COLUMN transaction_account_id VARCHAR(100);
ALTER TABLE otp_challenges ADD COLUMN destination_account_id VARCHAR(100);
ALTER TABLE otp_challenges ADD COLUMN transaction_operation VARCHAR(20);
ALTER TABLE otp_challenges ADD COLUMN transaction_amount NUMERIC(19,4);
