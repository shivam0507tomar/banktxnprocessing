# Bank Transaction Processor

A lightweight Spring Boot application for creating bank accounts and processing basic banking transactions in memory.

## Overview

This project exposes a REST API for:

- Creating account records
- Checking current balance
- Viewing transaction history
- Depositing funds
- Withdrawing funds
- Transferring funds between accounts

The implementation keeps account data in an in-memory repository, so it is suitable for demo use or local testing rather than production persistence.

## Tech Stack

- Java 21
- Spring Boot 4.2.0-M2
- Maven wrapper
- Jakarta Validation

## Project Structure

```text
src/
  main/
    java/com/example/banktxnprocessor/
      controller/
      dto/
      exception/
      model/
      repository/
      service/
  test/
```

## Prerequisites

- JDK 21+
- Maven or the included Maven wrapper

## Run the Application

On macOS/Linux:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
./mvnw.cmd spring-boot:run
```

The application starts on the default Spring Boot port:

```text
http://localhost:8080
```

## API Endpoints

### Create account

```http
POST /api/accounts/{id}
```

Response example:

```json
{
  "accountId": "acct-1001"
}
```

### Get account balance

```http
GET /api/accounts/{id}/balance
```

Response example:

```json
{
  "balance": 250.00
}
```

### Get transaction history

```http
GET /api/accounts/{id}/transactions
```

### Deposit funds

```http
POST /api/accounts/{id}/deposit
Content-Type: application/json
```

Request body:

```json
{
  "amount": 100.50
}
```

Response:

```json
{
  "status": "DEPOSIT_COMPLETED"
}
```

### Withdraw funds

```http
POST /api/accounts/{id}/withdraw
Content-Type: application/json
```

Request body:

```json
{
  "amount": 25.00
}
```

Response:

```json
{
  "status": "WITHDRAWAL_COMPLETED"
}
```

### Transfer between accounts

```http
POST /api/transfers
Content-Type: application/json
```

Request body:

```json
{
  "fromAccountId": "acct-1001",
  "toAccountId": "acct-1002",
  "amount": 75.25
}
```

Response:

```json
{
  "status": "TRANSFER_COMPLETED"
}
```

## Validation Rules

- Amounts must be greater than zero
- Deposit and withdrawal requests require a non-null amount value
- Transfers must use different source and destination accounts
- Withdrawal fails if the source account does not have enough funds

## Notes

- Account and ledger data are stored only in memory
- The application is designed for local development, testing, and demonstration
- No external database or persistence layer is configured
