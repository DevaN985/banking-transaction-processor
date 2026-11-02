-- Step 2: customer profile created after KYC onboarding.
CREATE TABLE customers (
    customer_id VARCHAR2(36 CHAR) NOT NULL,
    first_name VARCHAR2(80 CHAR) NOT NULL,
    middle_name VARCHAR2(80 CHAR),
    last_name VARCHAR2(80 CHAR) NOT NULL,
    date_of_birth DATE NOT NULL,
    email VARCHAR2(254 CHAR) NOT NULL,
    mobile_number VARCHAR2(20 CHAR) NOT NULL,
    address_line1 VARCHAR2(120 CHAR) NOT NULL,
    address_line2 VARCHAR2(120 CHAR),
    city VARCHAR2(60 CHAR) NOT NULL,
    state VARCHAR2(60 CHAR) NOT NULL,
    postal_code VARCHAR2(15 CHAR) NOT NULL,
    country VARCHAR2(2 CHAR) NOT NULL,
    kyc_reference VARCHAR2(100 CHAR) NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_customers PRIMARY KEY (customer_id),
    CONSTRAINT uk_customer_kyc_ref UNIQUE (kyc_reference),
    CONSTRAINT uk_customer_email UNIQUE (email)
);
