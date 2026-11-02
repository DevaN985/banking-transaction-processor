-- Step 4: immutable transaction ledger entries.
CREATE TABLE transactions (
    transaction_id VARCHAR2(36 CHAR) NOT NULL,
    account_id VARCHAR2(100 CHAR) NOT NULL,
    type VARCHAR2(30 CHAR) NOT NULL,
    amount NUMBER(19,4) NOT NULL,
    timestamp TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    transfer_id VARCHAR2(36 CHAR),
    CONSTRAINT pk_transactions PRIMARY KEY (transaction_id),
    CONSTRAINT ck_transactions_amount_positive CHECK (amount > 0)
);
