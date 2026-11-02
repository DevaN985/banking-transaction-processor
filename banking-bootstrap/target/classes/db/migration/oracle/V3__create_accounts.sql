-- Step 3: account balance and optimistic-lock version.
CREATE TABLE accounts (
    account_id VARCHAR2(100 CHAR) NOT NULL,
    balance NUMBER(19,4) NOT NULL,
    version NUMBER(19,0) NOT NULL,
    CONSTRAINT pk_accounts PRIMARY KEY (account_id),
    CONSTRAINT ck_accounts_balance_nonnegative CHECK (balance >= 0)
);
