-- Step 4: immutable transaction ledger entries.
CREATE TABLE transactions (
    transaction_id VARCHAR(36) NOT NULL,
    account_id VARCHAR(100) NOT NULL,
    type VARCHAR(30) NOT NULL,
    amount NUMERIC(19,4) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    transfer_id VARCHAR(36),
    CONSTRAINT pk_transactions PRIMARY KEY (transaction_id),
    CONSTRAINT ck_transactions_amount_positive CHECK (amount > 0)
);
