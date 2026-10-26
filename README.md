# Payment Reconciliation Engine

[![Maven CI](https://github.com/ChoudharyUday212000/payment-reconciliation-engine/actions/workflows/maven.yml/badge.svg)](https://github.com/ChoudharyUday212000/payment-reconciliation-engine/actions/workflows/maven.yml)

A testable Java 17 domain engine that reconciles a payment-gateway snapshot against an internal ledger snapshot. It is a portfolio project, not a connector to a real provider.

## Why it matters

Payment systems can see retries, delayed events, duplicated rows and partially posted transactions. Reconciliation should surface these mismatches instead of treating every missing row as a successful payment.

## Reconciliation outcomes

| Status | Rule |
| --- | --- |
| MATCHED | Exactly one record on each side, amount and currency match |
| MISSING_IN_GATEWAY | Ledger record exists, gateway record does not |
| MISSING_IN_LEDGER | Gateway record exists, ledger record does not |
| AMOUNT_MISMATCH | Both exist once, amounts differ |
| CURRENCY_MISMATCH | Both exist once, currencies differ |
| DUPLICATE_GATEWAY | More than one gateway row has the same reference |
| DUPLICATE_LEDGER | More than one ledger row has the same reference |
| DUPLICATE_IN_BOTH | Both sides contain duplicates for the reference |

## Design

- Java records provide immutable domain models with input validation.
- Amounts use integer minor units (paise/cents), not floating-point values.
- Records are grouped by external reference; report ordering is deterministic.
- Duplicate rows are retained for investigation.
- The signed difference is ledger amount minus gateway amount.

## Run

Requirements: JDK 17 and Maven 3.9+.

~~~bash
mvn test
mvn package
java -jar target/payment-reconciliation-engine-1.0.0.jar
~~~

The root repository includes GitHub Actions to run Maven verification on pushes and pull requests.

## Scope and next steps

The engine compares in-memory snapshots only. It does not fetch gateway reports, persist runs, schedule jobs or move funds. Production integrations would need file/API adapters, durable audit history, batch idempotency, metrics, alerts, currency-specific validation and human approval for any financial action.
