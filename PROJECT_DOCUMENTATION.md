# Banking Transaction Processor — Project Documentation

## 1. Purpose and scope

This project exposes banking operations through a Spring Boot REST application. It separates transport, application orchestration, domain behavior, persistence ports/adapters, and bootstrap configuration into Maven modules. This document describes behavior evidenced by the uploaded source; it does not introduce new business rules.

## 2. Architecture and module boundaries

### `banking-pojo`

Contains API DTOs under `com.example.banking.dto.request` and `.response`. Request DTOs carry Jakarta Bean Validation constraints such as required fields, size limits, numeric precision, and OTP format. Response DTOs shape API output and map domain objects where applicable.

### `banking-domain`

Contains persistent domain models and enums. `Account` owns balance mutation rules: opening balance cannot be negative, deposits/withdrawals must be positive, and withdrawal cannot exceed available balance. `Transaction` represents ledger history. `OtpChallenge` stores challenge identity, purpose, expiry, attempt count, consumption state, and optional onboarding/transaction context. `Customer` and `GovernmentProof` represent customer profile and verified government-proof data.

### `banking-application`

Contains service orchestration and repository interfaces. `TransactionService` creates accounts, retrieves accounts/history, deposits, withdraws, and transfers. `TransactionAuthorizationService` handles OTP initiation and confirmation for protected transactions. `GovernmentProofService` handles government-proof operations and bulk loading.

### `banking-adapters`

Contains REST controllers, JPA repository adapters, email delivery adapter, OpenAPI configuration, exception mapping, and reusable utilities. `AccountIdGenerator` generates candidate 15-digit account IDs and checks for collisions through `TransactionService`. `OtpUtils` provides OTP generation, hashing, validation, lifecycle checks, and email masking.

### `banking-bootstrap`

Contains `BankingApplication`, profile-specific configuration, and Flyway migration scripts for H2 and Oracle.

## 3. Persistence and profiles

The project includes profile configuration for `memory`, `oracle`, `file`, `mysql`, and `postgres`. H2 memory is intended for ephemeral local/test data. Oracle is the durable database profile. Database migrations are separated into H2 and Oracle directories under `banking-bootstrap/src/main/resources/db/migration`.

Do not assume profile parity solely from successful startup: run migration and persistence integration tests against each supported database engine. SQL types and locking behavior can differ.

## 4. Main flows

### Customer onboarding

1. Client initiates onboarding using government ID type and number.
2. Application looks up the trusted government-proof record.
3. Application generates an account identifier and OTP challenge.
4. OTP is sent to the email associated with the trusted KYC record.
5. Confirmation accepts challenge identifier and OTP.
6. Successful confirmation creates the customer/account relationship; opening balance is zero.

Purpose-specific checks and challenge lifecycle validation are kept distinct. The project does not call an external KYC provider in the bulk-load flow described by current source.

### Deposit

1. Service obtains the account under a write lock.
2. Domain validates the amount is positive and updates the balance.
3. A deposit transaction record is persisted.

### Withdrawal

1. The application determines whether OTP authorization is required based on the configured INR 1,000 threshold.
2. For OTP-protected withdrawals, the challenge stores account, operation, and amount; the recipient email is loaded from persisted customer data.
3. Confirmation validates challenge existence, reference, details, lifecycle, attempts, OTP, and purpose.
4. The withdrawal service validates funds, updates balance, persists a transaction, and consumes the challenge.

### Transfer

1. Transfer request validates source/destination requirements and amount.
2. For OTP-protected transfers, destination and amount are stored in the challenge.
3. Confirmation validates OTP lifecycle and transfer purpose.
4. Transaction service locks accounts in stable identifier order, checks funds, updates both balances, and records paired transfer-out/transfer-in entries sharing a transfer ID.

## 5. Transaction integrity

- Domain balance methods reject zero/negative amounts.
- Withdrawal and transfer prevent overdrafts.
- Transfer rejects missing/equal account identifiers.
- Account retrieval for mutation uses pessimistic write locking.
- Transfer ledger entries are linked by a transfer identifier.
- OTP challenges track attempts and consumed/expiry state.

These controls should be complemented by integration tests for rollback, concurrent requests, and actual database locking semantics.

## 6. API surface

Controllers in `banking-adapters` expose account, government-proof, and transaction endpoints. Exact request/response contracts should be treated as the controller DTOs and OpenAPI definition in the source. Swagger UI is available at `/swagger-ui/index.html` while the app is running.

## 7. Test strategy

### Unit tests included/expanded in this update

- `AccountTest`: valid construction, invalid ID/opening balance/customer ID, deposits, withdrawals, invalid amounts, overdraft protection, and balance invariance after failure.
- `OtpChallengeTest`: constructor state for onboarding/transaction challenges, getters, attempt increments, and consumption.
- `OtpUtilsTest`: six-digit generation, deterministic hashing, valid/invalid/blank/null OTP, consumed/expired/max-attempt lifecycle, and email masking edge cases.
- `AccountIdGeneratorTest`: available candidate, collision retry, and maximum retry exhaustion.
- `TransactionServiceTest`: deposit ledger entry, invalid amounts, transfer balance movement and paired ledger records, and failed-transfer state preservation.

### Coverage measurement

JaCoCo is configured in the root Maven build. Run `mvn clean verify` and inspect each module's HTML report. **80%+ is the requested target, not a verified current metric** until the build has run successfully and the reports have been inspected. Branch coverage should be reviewed for OTP and money-movement logic independently from line coverage.

## 8. Operational commands

```powershell
mvn clean test
mvn clean verify
mvn -f banking-bootstrap\pom.xml spring-boot:run
```

Set `SPRING_PROFILES_ACTIVE` in the shell to select the profile without editing property files.

## 9. Verification limits and next quality gates

The current execution environment did not have Maven available, so this documentation/test update has not been compiled or run here. Before merging, run the full Maven reactor locally and confirm: tests pass, JaCoCo line coverage reaches 80%+, critical branch coverage is reviewed, and H2/Oracle integration tests pass. No claim of achieved coverage should be made until that evidence exists.
