-- Step 2: customer profile created after KYC onboarding.
CREATE TABLE customers (
    customer_id VARCHAR(36) NOT NULL,
    first_name VARCHAR(80) NOT NULL,
    middle_name VARCHAR(80),
    last_name VARCHAR(80) NOT NULL,
    date_of_birth DATE NOT NULL,
    email VARCHAR(254) NOT NULL,
    mobile_number VARCHAR(20) NOT NULL,
    address_line1 VARCHAR(120) NOT NULL,
    address_line2 VARCHAR(120),
    city VARCHAR(60) NOT NULL,
    state VARCHAR(60) NOT NULL,
    postal_code VARCHAR(15) NOT NULL,
    country VARCHAR(2) NOT NULL,
    kyc_reference VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_customers PRIMARY KEY (customer_id),
    CONSTRAINT uk_customer_kyc_ref UNIQUE (kyc_reference),
    CONSTRAINT uk_customer_email UNIQUE (email)
);
