-- Persist onboarding context with OTP and link newly created accounts to customers.
ALTER TABLE otp_challenges ADD (
    kyc_reference VARCHAR2(36 CHAR),
    requested_account_id VARCHAR2(100 CHAR),
    opening_balance NUMBER(19,4)
);

ALTER TABLE accounts ADD customer_id VARCHAR2(36 CHAR);

ALTER TABLE customers MODIFY (
    mobile_number NULL,
    address_line1 NULL,
    city NULL,
    state NULL,
    postal_code NULL,
    country NULL
);

ALTER TABLE accounts ADD CONSTRAINT fk_accounts_customer
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id);
