# SEPA Instant Payment & ISO 20022

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/technologies/javase/21-ea-downloads.html)
[![Spring Boot 3.2.x](https://img.shields.io/badge/Spring_Boot-3.2.x-6DB33F.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud Gateway](https://img.shields.io/badge/Spring_Cloud_Gateway-2023.0.1-6DB33F.svg)](https://spring.io/projects/spring-cloud-gateway)
[![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-2.x-231F20.svg)](https://kafka.apache.org/)
[![Maven](https://img.shields.io/badge/Build_with-Maven-CC2200.svg)](https://maven.apache.org/)
[![Docker](https://img.shields.io/badge/Containerized_with-Docker-2496ED.svg)](https://www.docker.com/)


> Event-Driven SEPA Instant Payment and ISO 20022 Showcase
> Built as a software architecture portfolio project demonstrating modern payment processing, settlement, reporting, and operational resilience concepts.

---

## Overview

SEPA Instant Payment Gateway is a showcase project that simulates the complete lifecycle of a European credit transfer using ISO 20022 messages.

The system demonstrates:

* SEPA Credit Transfer (SCT)
* SEPA Instant Credit Transfer (SCT Inst)
* pacs.008 payment initiation
* pacs.002 payment status reporting
* pacs.004 payment return
* camt.052 intraday reporting
* camt.053 end-of-day statement
* camt.054 debit/credit notification

The project focuses on software architecture, event-driven processing, observability, resiliency, and auditability rather than banking product functionality.

---

## Architecture Goals

* Demonstrate ISO 20022 payment processing concepts.
* Showcase event-driven architecture using Kafka.
* Implement payment orchestration and settlement simulation.
* Provide immutable audit trails.
* Demonstrate operational resilience patterns.
* Simulate European payment reporting standards.
* Serve as a portfolio project for banking and payment architecture roles.

---

## Features

### Payment Processing

* Credit transfer initiation
* Payment validation
* Idempotent payment requests
* Payment status tracking
* Payment return processing

### ISO 20022 Messages

* pacs.008
* pacs.002
* pacs.004
* camt.052
* camt.053
* camt.054

### Architecture Features

* Event-driven architecture
* Kafka-based messaging
* Correlation IDs
* Immutable audit logs
* Retry and DLQ
* OAuth2/JWT security
* Distributed tracing support

### Operational Features

* Payment monitoring
* Exception handling
* Audit trail
* Reporting generation
* Backoffice APIs

---

## Technology Stack

| Area          | Technology            |
| ------------- | --------------------- |
| Language      | Java 21               |
| Framework     | Spring Boot           |
| Build Tool    | Maven                 |
| Messaging     | Kafka                 |
| Database      | PostgreSQL            |
| Security      | OAuth2 / JWT          |
| IAM           | Keycloak              |
| Container     | Docker                |
| Orchestration | Kubernetes (optional) |
| Cache         | Redis                 |
| API           | REST                  |

---

## Architecture

The system follows an event-driven architecture.

```text
Client
   |
API Gateway
   |
Payment Service
   |
Kafka
   |
+-------------------+
| Settlement Service|
| Ledger Service    |
| Reporting Service |
+-------------------+
```

The architecture emphasizes:

* loose coupling
* asynchronous processing
* observability
* resiliency
* auditability

Detailed documentation is available in:

```text
docs/arc42.md
```

---

## Payment Flow

```text
Payment Request
      |
      v
pacs.008 generated
      |
      v
Kafka Event
      |
      v
Settlement Processing
      |
      v
pacs.002 generated
      |
      v
Ledger Update
      |
      v
camt Reporting
```

---

## Example ISO 20022 Messages

The repository includes example messages:

```text
samples/
├── pacs008.xml
├── pacs002-accepted.xml
├── pacs002-rejected.xml
├── pacs004-return.xml
├── camt052.xml
├── camt053.xml
└── camt054.xml
```

---

## Repository Structure

```text
sepa-instant-gateway/
|
├── docs/
├── services/
├── samples/
├── postman/
├── docker-compose.yml
└── README.md
```

---

## Running Locally

### Start infrastructure

```bash
docker compose up -d
```

Infrastructure:

* PostgreSQL
* Kafka
* Redis
* Keycloak

### Run services

```bash
mvn spring-boot:run
```

or

```bash
docker compose up
```

---

## Quality Attributes

* Reliability
* Auditability
* Observability
* Security
* Extensibility
* Consistency

---

## Resilience Patterns

* Idempotency Key
* Retry with Backoff
* Dead Letter Queue
* Immutable Audit Trail
* Correlation ID
* Event Replay

---

## Security

* OAuth2 authentication
* JWT authorization
* Role-based access control
* API protection
* Audit logging

---

## Disclaimer

This project is a technical showcase.

It does not connect to:

* TIPS
* RT1
* TARGET Services
* SWIFT
* Real banking networks

All settlement and clearing behavior is simulated for educational and architectural purposes.

---

## Architecture Documentation

* arc42 documentation
* Architecture Decision Records
* Runtime scenarios
* Deployment diagrams
* Sequence diagrams

See:

```text
docs/arc42.md
```

---

## Author

Slamet Widodo

Software Architect specializing in:

* Banking Platforms
* Payment Systems
* ISO 8583
* ISO 20022
* Event-Driven Architecture
* Cloud-Native Systems
* Java and Spring Boot
* Financial System Integration

---

## License

MIT License
