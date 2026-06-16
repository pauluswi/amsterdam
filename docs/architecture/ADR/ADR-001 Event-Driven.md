# ADR-001: Event-Driven Architecture with Kafka

## Status
Accepted

## Context

The SEPA payment showcase must handle multiple asynchronous workflows:
- Payment initiation triggers settlement processing
- Settlement results trigger ledger updates
- Ledger changes trigger reporting
- These processes may fail independently and need retry/recovery

A synchronous, request-response architecture would create tight coupling between services and make recovery difficult.

## Decision

Use Apache Kafka as the event backbone to decouple payment, settlement, ledger, and reporting services.

Each service publishes domain events to Kafka topics and consumes events from other services asynchronously.

## Rationale

1. **Loose Coupling**: Services don't depend on each other's availability or response time
2. **Resilience**: Events persist in Kafka; failed consumers can retry independently
3. **Auditability**: Complete event log provides audit trail for compliance
4. **Scalability**: Easy to add new consumers (e.g., additional reporting services)
5. **Replay**: Operational recovery by replaying events if needed
6. **Banking Standard**: Event sourcing aligns with payment processing best practices

## Consequences

### Positive
- Services are independently deployable and scalable
- Clear separation of concerns
- Event history provides compliance audit trail
- Easy to add new event consumers without modifying existing services

### Negative
- Eventual consistency instead of strong consistency (acceptable for reporting, requires care for ledger)
- Operational complexity: Kafka cluster management, monitoring, DLQ handling
- Debugging distributed flows is harder
- Requires idempotency and correlation IDs to handle duplicate events

## Implementation Notes

- **Topics**: `payment.initiated`, `payment.status`, `payment.returned`, `ledger.posted`, `reporting.generated`
- **Consumer Groups**: Each service has its own consumer group for independent offset management
- **Dead Letter Queue**: Failed messages go to DLQ for manual investigation
- **Correlation ID**: Every event carries `correlationId` for tracing across services

## Alternatives Considered

1. **Synchronous REST calls**: Simpler initially, but creates cascading failures
2. **Message queue (RabbitMQ)**: Less suitable for event replay scenarios
3. **Event store (Event Sourcing DB)**: Over-engineered for showcase scope

## References

- Kafka documentation: https://kafka.apache.org/documentation/
- Event-driven architecture: https://martinfowler.com/articles/201701-event-driven.html
