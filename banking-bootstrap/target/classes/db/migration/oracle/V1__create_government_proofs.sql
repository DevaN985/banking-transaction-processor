-- Step 1: trusted government identity proof records.
-- Do not edit after this migration has been applied.
CREATE TABLE government_proofs (
    id VARCHAR2(36 CHAR) NOT NULL,
    government_id_type VARCHAR2(30 CHAR) NOT NULL,
    government_id_number VARCHAR2(30 CHAR) NOT NULL,
    first_name VARCHAR2(80 CHAR) NOT NULL,
    middle_name VARCHAR2(80 CHAR),
    last_name VARCHAR2(80 CHAR) NOT NULL,
    date_of_birth DATE NOT NULL,
    validation_status VARCHAR2(30 CHAR) NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_government_proofs PRIMARY KEY (id),
    CONSTRAINT uk_gov_proof_type_number UNIQUE (government_id_type, government_id_number)
);
