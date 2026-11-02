-- Step 3: account balance and optimistic-lock version.
CREATE TABLE accounts (
    account_id VARCHAR(100) NOT NULL,
    balance NUMERIC(19,4) NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT pk_accounts PRIMARY KEY (account_id),
    CONSTRAINT ck_accounts_balance_nonnegative CHECK (balance >= 0)
);
