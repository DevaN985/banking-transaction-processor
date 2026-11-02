-- Step 1: trusted government identity proof records.
-- Do not edit after this migration has been applied.
CREATE TABLE government_proofs (
    id VARCHAR(36) NOT NULL,
    government_id_type VARCHAR(30) NOT NULL,
    government_id_number VARCHAR(30) NOT NULL,
    first_name VARCHAR(80) NOT NULL,
    middle_name VARCHAR(80),
    last_name VARCHAR(80) NOT NULL,
    date_of_birth DATE NOT NULL,
    validation_status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_government_proofs PRIMARY KEY (id),
    CONSTRAINT uk_gov_proof_type_number UNIQUE (government_id_type, government_id_number)
);
