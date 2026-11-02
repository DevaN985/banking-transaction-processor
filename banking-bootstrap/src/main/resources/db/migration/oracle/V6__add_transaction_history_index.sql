-- Step 6: optimize account transaction history queries ordered by timestamp.
CREATE INDEX idx_tx_account_timestamp ON transactions (account_id, timestamp);
