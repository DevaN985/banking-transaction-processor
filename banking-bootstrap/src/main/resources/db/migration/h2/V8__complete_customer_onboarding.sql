-- Persist onboarding context with OTP and link newly created accounts to customers.
ALTER TABLE otp_challenges ADD kyc_reference VARCHAR(36);
ALTER TABLE otp_challenges ADD requested_account_id VARCHAR(100);
ALTER TABLE otp_challenges ADD opening_balance NUMERIC(19,4);

ALTER TABLE accounts ADD customer_id VARCHAR(36);

ALTER TABLE customers ALTER COLUMN mobile_number DROP NOT NULL;
ALTER TABLE customers ALTER COLUMN address_line1 DROP NOT NULL;
ALTER TABLE customers ALTER COLUMN city DROP NOT NULL;
ALTER TABLE customers ALTER COLUMN state DROP NOT NULL;
ALTER TABLE customers ALTER COLUMN postal_code DROP NOT NULL;
ALTER TABLE customers ALTER COLUMN country DROP NOT NULL;

ALTER TABLE accounts ADD CONSTRAINT fk_accounts_customer
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id);
