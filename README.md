# Banking Transaction Processor

A Java 17, Spring Boot 3.3.5, Maven multi-module banking API using a hexagonal/layered separation. This repository supports account operations, customer onboarding backed by government-proof records, OTP-authorized higher-value transactions, transaction history, and database profiles.

## Modules

| Module | Responsibility |
|---|---|
| `banking-pojo` | API request/response DTOs and validation annotations |
| `banking-domain` | JPA domain entities and account balance rules |
| `banking-application` | Services and repository ports; transaction orchestration |
| `banking-adapters` | REST controllers, persistence adapters, email adapter, utilities, OpenAPI |
| `banking-bootstrap` | Spring Boot entry point, profile configuration, Flyway migrations |

## Prerequisites

- JDK 17
- Maven 3.9+
- Oracle 19c for durable Oracle profile; H2 is used for the memory profile

## Build and run

From the repository root:

```powershell
mvn clean test
mvn -f banking-bootstrap\pom.xml spring-boot:run
```

The default profile is configured in `banking-bootstrap/src/main/resources/application.properties`. Select a profile without editing files:

```powershell
$env:SPRING_PROFILES_ACTIVE = 'memory'
mvn -f banking-bootstrap\pom.xml spring-boot:run
```

Oracle profile (configure credentials using your existing local environment/configuration):

```powershell
$env:SPRING_PROFILES_ACTIVE = 'oracle'
mvn -f banking-bootstrap\pom.xml spring-boot:run
```

Other profiles present in the project: `file`, `mysql`, `postgres`. Review the matching profile configuration and migration compatibility before using them.

## API documentation

When the application is running, Swagger UI is available at:

`http://localhost:8080/swagger-ui/index.html`

OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Test suite and coverage

Tests use JUnit 5, Spring Boot Test, and Mockito. Tests are placed under each module's Maven-standard `src/test/java` tree. Coverage is instrumented with JaCoCo.

```powershell
mvn clean verify
```

JaCoCo HTML reports are generated per module at `MODULE/target/site/jacoco/index.html`. Review line and branch coverage separately. The project goal is **at least 80% line coverage**, with critical money movement and OTP paths also requiring branch coverage. A target is not a measured result: run the command above and inspect the report before claiming the threshold is achieved.

## Key behavior documented in source

- Account opening starts with government ID type/number, creates an internally generated account ID, and uses OTP confirmation for onboarding.
- New accounts have a zero opening balance.
- Deposits execute without OTP. Withdrawals and transfers above INR 1,000 require OTP; amounts at or below the threshold do not require OTP according to the current application flow.
- Transaction OTP challenges store transaction details, so confirmation does not need the client to resubmit those details.
- OTP is sent to the registered customer email retrieved from persisted customer/account data.
- Account balance operations reject non-positive amounts and prevent overdrafts. Transfers create linked ledger entries and use pessimistic account locking.

See [`PROJECT_DOCUMENTATION.md`](PROJECT_DOCUMENTATION.md) for module-level detail, flows, test scope, and known verification limits.

## Important

This test/documentation update is intended to preserve existing behavior. It does not replace security review, integration testing against Oracle, concurrency testing, or API contract testing.
