-- Add trusted email destination for KYC OTP. Existing rows must be backfilled before onboarding.
ALTER TABLE government_proofs ADD registered_email VARCHAR2(254 CHAR);
