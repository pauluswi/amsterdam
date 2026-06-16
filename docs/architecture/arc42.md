# arc42 Architecture Documentation

# SEPA Instant Payment & ISO 20022 Reporting Showcase

## 1. Introduction and Goals

### 1.1 Purpose

This project demonstrates an end-to-end European payment processing showcase using ISO 20022 messages.

The system simulates:

* SEPA Instant Credit Transfer flow
* pacs.008 payment initiation
* pacs.002 payment status reporting
* pacs.004 payment return
* camt.052 intraday account report
* camt.053 end-of-day statement
* camt.054 credit/debit notification

The purpose is to show practical architecture capability in payment orchestration, clearing simulation, settlement ledger, exception handling, and ISO 20022 message generation.

### 1.2 Business Goals

| Goal                       | Description                                                       |
| -------------------------- | ----------------------------------------------------------------- |
| European payment readiness | Demonstrate understanding of SEPA-style payment flows             |
| ISO 20022 capability       | Generate and process pacs and camt messages                       |
| Payment lifecycle coverage | Cover initiation, status, return, reporting, and notification     |
| Architecture showcase      | Provide a portfolio-ready system for banking and fintech roles    |
| Event-driven design        | Use Kafka to decouple payment, settlement, and reporting services |

### 1.3 Quality Goals

| Quality Attribute | Goal                                                      |
| ----------------- | --------------------------------------------------------- |
| Reliability       | Payment status must be traceable and recoverable          |
| Consistency       | Ledger and payment status must remain consistent          |
| Auditability      | Every payment event must be recorded                      |
| Extensibility     | New message types should be easy to add                   |
| Observability     | Every transaction must have correlation ID and trace logs |
| Security          | API access must be protected using OAuth2/JWT             |

---

## 2. Architecture Constraints

| Constraint       | Description                                                            |
| ---------------- | ---------------------------------------------------------------------- |
| Message standard | ISO 20022 XML messages                                                 |
| Main language    | Java 21                                                                |
| Framework        | Spring Boot                                                            |
| Build tool       | Maven                                                                  |
| Messaging        | Kafka                                                                  |
| Database         | PostgreSQL                                                             |
| Containerization | Docker                                                                 |
| API style        | REST API                                                               |
| Authentication   | OAuth2/JWT                                                             |
| Deployment       | Docker Compose for local showcase, Kubernetes optional                 |
| Scope            | Simulation only, not connected to real SEPA, SWIFT, or TARGET services |

---

## 3. System Scope and Context

### 3.1 Business Context

```text
+----------------+        +-----------------------------+
| Corporate User | -----> | SEPA Payment Gateway        |
+----------------+        +-----------------------------+
                                      |
                                      v
                         +-----------------------------+
                         | Settlement Simulator        |
                         +-----------------------------+
                                      |
                                      v
                         +-----------------------------+
                         | Reporting & Notification    |
                         +-----------------------------+
```

### 3.2 External Actors

| Actor                | Description                              |
| -------------------- | ---------------------------------------- |
| Corporate User       | Initiates payment and requests reports   |
| Debtor Bank          | Simulated sending bank                   |
| Creditor Bank        | Simulated receiving bank                 |
| Settlement Simulator | Simulates clearing and settlement result |
| Backoffice User      | Reviews payment status and exceptions    |

### 3.3 Main Use Cases

| Use Case                           | ISO 20022 Message |
| ---------------------------------- | ----------------- |
| Send credit transfer               | pacs.008          |
| Receive payment status             | pacs.002          |
| Return settled payment             | pacs.004          |
| Generate intraday report           | camt.052          |
| Generate end-of-day statement      | camt.053          |
| Generate debit/credit notification | camt.054          |

---

## 4. Solution Strategy

The solution uses event-driven architecture to separate payment initiation, settlement processing, ledger updates, and reporting.

Main strategy:

1. Payment API receives payment instruction.
2. Payment Service validates request and generates pacs.008.
3. Payment event is published to Kafka.
4. Settlement Service consumes pacs.008 and produces pacs.002.
5. Ledger Service records debit, credit, and settlement result.
6. Return Service can generate pacs.004 for returned payments.
7. Reporting Service generates camt.052, camt.053, and camt.054 from ledger entries.

Key design principles:

* API-first design
* Idempotency for payment requests
* Event-driven payment lifecycle
* Immutable audit trail
* ISO 20022 XML generation layer
* Clear separation between orchestration and reporting

---

## 5. Building Block View

### 5.1 Level 1 - System Overview

```text
+------------------------------------------------------+
| SEPA Instant Payment & Reporting Showcase            |
+------------------------------------------------------+
| Payment API                                          |
| Payment Orchestration Service                        |
| Settlement Simulator                                 |
| Ledger Service                                       |
| Return Service                                       |
| Reporting Service                                    |
| ISO 20022 Message Generator                          |
| Backoffice API                                       |
+------------------------------------------------------+
```

### 5.2 Level 2 - Containers

```text
+----------------+       +------------------+
| API Gateway    | ----> | Payment Service  |
+----------------+       +------------------+
                                 |
                                 v
                          +-------------+
                          | Kafka       |
                          +-------------+
                           |     |     |
              +------------+     |     +-------------+
              v                  v                   v
+--------------------+   +----------------+   +----------------+
| Settlement Service |   | Ledger Service |   | Reporting Svc  |
+--------------------+   +----------------+   +----------------+
              |                  |                   |
              v                  v                   v
        pacs.002           PostgreSQL           camt reports
```

### 5.3 Main Components

| Component           | Responsibility                                                |
| ------------------- | ------------------------------------------------------------- |
| Payment API         | Exposes REST endpoints for payment creation and inquiry       |
| Payment Service     | Validates payment, creates payment record, generates pacs.008 |
| ISO 20022 Generator | Converts internal payment model into ISO 20022 XML            |
| Settlement Service  | Simulates clearing result and generates pacs.002              |
| Return Service      | Handles return scenario and generates pacs.004                |
| Ledger Service      | Maintains account balance and accounting entries              |
| Reporting Service   | Generates camt.052, camt.053, camt.054                        |
| Audit Service       | Stores payment lifecycle events                               |
| Backoffice API      | Provides payment monitoring and exception view                |

---

## 6. Runtime View

### 6.1 Credit Transfer Flow - pacs.008 and pacs.002

```text
Corporate User
    |
    | POST /payments
    v
Payment API
    |
    | Validate request
    | Generate paymentId
    | Generate pacs.008
    v
Payment Service
    |
    | Publish PaymentInitiated event
    v
Kafka topic: payment.initiated
    |
    v
Settlement Service
    |
    | Validate debtor/creditor bank
    | Simulate settlement
    | Generate pacs.002
    v
Kafka topic: payment.status
    |
    v
Payment Service
    |
    | Update status: ACCEPTED / REJECTED / PENDING
    v
Ledger Service
    |
    | Record debit and credit entries
```

### 6.2 Payment Return Flow - pacs.004

```text
Backoffice User
    |
    | POST /payments/{paymentId}/return
    v
Return Service
    |
    | Validate original payment
    | Check settlement status
    | Generate pacs.004
    v
Kafka topic: payment.returned
    |
    v
Ledger Service
    |
    | Reverse ledger entries
    | Record return transaction
    v
Payment Service
    |
    | Update payment status: RETURNED
```

### 6.3 Reporting Flow - camt.052 / camt.053 / camt.054

```text
Ledger Service
    |
    | Ledger entries
    v
Reporting Service
    |
    +--> Generate camt.052 intraday report
    |
    +--> Generate camt.053 end-of-day statement
    |
    +--> Generate camt.054 debit/credit notification
```

---

## 7. Deployment View

### 7.1 Local Deployment

```text
Docker Compose
|
+-- payment-service
+-- settlement-service
+-- ledger-service
+-- reporting-service
+-- postgres
+-- kafka
+-- redis
+-- keycloak
```

### 7.2 Optional Kubernetes Deployment

```text
Kubernetes Cluster
|
+-- namespace: sepa-showcase
    |
    +-- payment-service deployment
    +-- settlement-service deployment
    +-- ledger-service deployment
    +-- reporting-service deployment
    +-- kafka statefulset
    +-- postgres statefulset
    +-- keycloak deployment
```

---

## 8. Cross-cutting Concepts

### 8.1 Idempotency

Every payment request must include an `Idempotency-Key`.

If the same request is submitted again, the system returns the existing payment result instead of creating duplicate payment instructions.

### 8.2 Correlation ID

Every request and event carries:

```text
correlationId
paymentId
messageId
endToEndId
instructionId
```

### 8.3 Audit Trail

Every lifecycle event is stored:

```text
PAYMENT_RECEIVED
PACS008_GENERATED
PAYMENT_ACCEPTED
PACS002_GENERATED
LEDGER_POSTED
PAYMENT_RETURNED
PACS004_GENERATED
CAMT054_GENERATED
```

### 8.4 ISO 20022 Message Generation

The system separates internal domain model from ISO 20022 XML.

```text
Payment Domain Model
        |
        v
ISO 20022 Mapper
        |
        v
XML Generator
        |
        v
pacs.008 / pacs.002 / pacs.004 / camt.*
```

### 8.5 Exception Handling

Common exception scenarios:

| Scenario                | Handling                                  |
| ----------------------- | ----------------------------------------- |
| Invalid IBAN            | Reject payment and generate pacs.002 RJCT |
| Duplicate payment       | Return existing payment result            |
| Insufficient balance    | Reject settlement                         |
| Creditor account closed | Generate pacs.004 return                  |
| Kafka failure           | Retry and DLQ                             |
| XML validation failure  | Mark message as failed                    |

---

## 9. Architecture Decisions

### ADR-001: Use Event-Driven Architecture with Kafka

Decision: Use Kafka as the backbone for payment lifecycle events.

Reason:

* Payment systems need asynchronous processing.
* Settlement and reporting should be decoupled.
* Retry and replay are important for operational recovery.

Consequences:

* More operational complexity.
* Requires correlation ID and idempotency.
* Better scalability and resilience.

### ADR-002: Service-Level Routing for SEPA Credit Transfer vs. SEPA Instant Credit Transfer

Decision: Add a `serviceLevel` field to the payment domain model with values `SEPA` or `INST`. The Settlement Service routes payment events to service-level-specific handlers based on this field.

Reason:

* Explicitly differentiate between SEPA Credit Transfer and SEPA Instant Credit Transfer.
* Ensure correct settlement timing and compliance with ISO 20022 standards.

Consequences:

* Clear, explicit routing eliminates ambiguity.
* Settlement Service has two code paths (slight complexity increase).

### ADR-003: Generate ISO 20022 XML from Internal Domain Model

Decision: The system will maintain a clean, internal domain model that represents the core business concepts. A dedicated ISO 20022 mapping and generation layer will be responsible for translating between this internal domain model and the external ISO 20022 XML messages.

Reason:

* Decouples business logic from external message serialization/deserialization.
* Improves maintainability, flexibility, and testability.

Consequences:

* Cleaner, more focused business logic.
* Requires an additional layer of code for mapping, increasing initial development effort.

### ADR-004: Use Keycloak for OAuth2/JWT Authentication and Authorization

Decision: Keycloak will be used as the Identity and Access Management (IAM) solution to provide OAuth2/JWT-based authentication and authorization for all exposed REST APIs.

Reason:

* Demonstrates modern enterprise security practices using industry standards.
* Provides robust authentication and authorization features.

Consequences:

* Secure API architecture.
* Adds an external dependency (Keycloak server) to the deployment environment.

### ADR-005: Use PostgreSQL as System of Record

Decision: PostgreSQL will be used as the primary system of record for all persistent data within the showcase application, including payment, ledger, and audit records.

Reason:

* Strong transactional consistency (ACID) crucial for financial applications.
* Maturity, reliability, and ease of local setup via Docker.

Consequences:

* Guaranteed data consistency for financial transactions.
* Requires careful design of transaction boundaries and potential scaling for high-volume production.

### ADR-006: Idempotency for Payment Requests

Decision: All payment initiation API endpoints will be designed to be idempotent. Clients will be required to provide a unique `Idempotency-Key` header with each payment request.

Reason:

* Prevents duplicate transactions, critical for financial systems.
* Allows clients to safely retry requests without unintended side effects.

Consequences:

* Eliminates the risk of duplicate payment processing.
* Adds complexity to server-side implementation and requires client-side key generation.

### ADR-007: Immutable Audit Trail

Decision: The system will implement an immutable audit trail for all significant payment lifecycle events. Once an audit record is created and persisted, it cannot be altered or deleted.

Reason:

* Ensures data integrity and trust, facilitating regulatory compliance.
* Simplifies debugging, forensics, and supports non-repudiation.

Consequences:

* High confidence in historical payment data.
* Increased storage requirements and potential performance overhead if not efficiently implemented.

### ADR-008: Correlation ID for Distributed Tracing

Decision: A unique `correlationId` will be generated at the entry point of every logical request and propagated consistently across all subsequent service calls and log entries. Domain-specific identifiers will also be propagated.

Reason:

* Enhances observability and simplifies debugging in a distributed microservices architecture.
* Improves monitoring and facilitates faster incident response.

Consequences:

* Significantly improves the ability to understand, debug, and monitor the distributed system.
* Requires consistent implementation across all services and adds a small overhead to message payloads.

### ADR-009: Centralized Exception Handling and Retry Mechanisms

Decision: The system will implement a centralized strategy for exception handling, incorporating retry mechanisms for transient failures and Dead Letter Queues (DLQs) for persistent failures. APIs will return standardized error responses.

Reason:

* Increases system resilience and ensures message processing guarantees.
* Improves operational efficiency and data consistency.

Consequences:

* Increased system resilience and fault tolerance.
* Adds complexity to consumer logic and requires careful configuration of retry policies and DLQ management.

---

## 10. Quality Requirements

| ID    | Scenario                                              | Expected Result                                |
| ----- | ----------------------------------------------------- | ---------------------------------------------- |
| QR-01 | Duplicate payment submitted with same idempotency key | System returns original payment result         |
| QR-02 | Settlement service is temporarily down                | Event remains in Kafka and is retried          |
| QR-03 | Invalid payment data submitted                        | System rejects and records audit event         |
| QR-04 | Payment accepted                                      | pacs.002 with accepted status is generated     |
| QR-05 | Payment returned                                      | pacs.004 is generated and ledger reversed      |
| QR-06 | Intraday report requested                             | camt.052 is generated from current ledger      |
| QR-07 | End-of-day report requested                           | camt.053 is generated from daily ledger        |
| QR-08 | Credit notification requested                         | camt.054 is generated for selected transaction |

---

## 11. Risks and Technical Debt

| Risk                 | Description                                   | Mitigation                                 |
| -------------------- | --------------------------------------------- | ------------------------------------------ |
| ISO 20022 complexity | Real message schema is large                  | Start with simplified but realistic subset |
| SEPA rulebook gap    | Showcase may not fully implement EPC rules    | Document assumptions clearly               |
| XML validation       | Incorrect XML may reduce credibility          | Add XSD validation tests                   |
| Overengineering      | Too many services may slow delivery           | Start modular monolith, then split         |
| Settlement realism   | Real TARGET/TIPS integration is not available | Use simulator and explain boundary         |

---

## 12. Glossary

| Term     | Meaning                           |
| -------- | --------------------------------- |
| pacs.008 | Customer credit transfer message  |
| pacs.002 | Payment status report             |
| pacs.004 | Payment return message            |
| camt.052 | Intraday account report           |
| camt.053 | End-of-day bank statement         |
| camt.054 | Debit/credit notification         |
| SEPA     | Single Euro Payments Area         |
| SCT      | SEPA Credit Transfer              |
| SCT Inst | SEPA Instant Credit Transfer      |
| TIPS     | TARGET Instant Payment Settlement |
| IBAN     | International Bank Account Number |
| BIC      | Bank Identifier Code              |
| E2E ID   | End-to-end payment reference      |
| DLQ      | Dead Letter Queue                 |

---

# Suggested Repository Structure

```text
sepa-instant-gateway/
|
+-- README.md
+-- docs/
|   +-- arc42.md
|   +-- c4-context.png
|   +-- c4-container.png
|   +-- sequence-pacs008-pacs002.png
|   +-- sequence-pacs004-return.png
|   +-- sequence-camt-reporting.png
|   +-- adr/
|       +-- adr-001-event-driven.md
|       +-- adr-002-service-level.md
|       +-- adr-003-iso20022-mapping.md
|       +-- adr-004-keycloak.md
|       +-- adr-005-postgresql.md
|       +-- adr-006-idempotency.md
|       +-- adr-007-immutable-audit-trail.md
|       +-- adr-008-correlation-id.md
|       +-- adr-009-exception-handling.md
|
+-- services/
|   +-- payment-service/
|   +-- settlement-service/
|   +-- ledger-service/
|   +-- reporting-service/
|
+-- docker-compose.yml
+-- postman/
+-- samples/
    +-- pacs008.xml
    +-- pacs002-accepted.xml
    +-- pacs002-rejected.xml
    +-- pacs004-return.xml
    +-- camt052.xml
    +-- camt053.xml
    +-- camt054.xml
```
