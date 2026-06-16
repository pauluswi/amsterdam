# ADR-009: Centralized Exception Handling and Retry Mechanisms

## Status
Accepted

## Context

In a distributed, event-driven microservices architecture, failures are inevitable. Services can become temporarily unavailable, network issues can cause transient communication errors, or messages can be malformed. Without robust mechanisms to handle these exceptions, the system can become brittle, leading to lost messages, inconsistent states, and operational headaches.

The `arc42.md` document explicitly lists "Exception Handling" as a cross-cutting concept and specifically mentions "Kafka failure: Retry and DLQ" in the "Risks and Technical Debt" section, highlighting the critical need for a well-defined strategy to manage failures and ensure message processing guarantees.

## Decision

The system will implement a centralized strategy for exception handling, incorporating:
1.  **Retry Mechanisms**: For transient failures (e.g., temporary network issues, database contention), services will implement configurable retry policies.
2.  **Dead Letter Queues (DLQs)**: For persistent failures (e.g., malformed messages, unrecoverable business logic errors), messages will be moved to a dedicated Dead Letter Queue for manual inspection and reprocessing.
3.  **Standardized Error Responses**: APIs will return standardized error responses, and internal services will log structured error information.

## Rationale

1.  **System Resilience**: Retry mechanisms allow services to recover gracefully from transient issues, preventing cascading failures and improving overall system uptime and availability.
2.  **Message Processing Guarantees**: DLQs ensure that no message is silently lost due to unrecoverable errors, providing a safety net for critical data and enabling manual intervention.
3.  **Operational Efficiency**: Centralized handling reduces boilerplate code, makes error patterns predictable, and simplifies monitoring and alerting for operational teams.
4.  **Data Consistency**: By handling failures systematically, the risk of inconsistent data states due to partial processing is minimized.
5.  **Improved Debugging**: Standardized error formats and DLQs provide clear insights into why a message failed, aiding in faster debugging and resolution.

## Consequences

### Positive
-   Increased system resilience and fault tolerance.
-   Guaranteed processing of critical messages (eventually, or via manual intervention).
-   Reduced manual effort for recovering from common transient failures.
-   Clear visibility into message processing failures and their causes.
-   More consistent and predictable behavior across services.

### Negative
-   Adds complexity to consumer logic and message processing pipelines.
-   Requires careful configuration of retry policies (e.g., number of retries, backoff strategy) to avoid overwhelming downstream services.
-   DLQs require dedicated monitoring, alerting, and a process for manual inspection and reprocessing of failed messages.
-   Potential for message reordering if retries are not handled carefully, which might need to be addressed depending on business requirements.
-   Increased infrastructure (e.g., additional Kafka topics for DLQs).

## Implementation Notes

-   **Retry Mechanisms**:
    -   **Kafka Consumers**: Utilize Spring Kafka's built-in retry mechanisms (e.g., `DefaultErrorHandler` with `FixedBackOff` or `ExponentialBackOff`) for transient exceptions.
    -   **API Clients**: Implement retry logic (e.g., using Spring's `RetryTemplate` or libraries like Resilience4j) for external API calls.
    -   **Idempotency**: Retries must be combined with idempotency (ADR-006) to prevent duplicate processing.
-   **Dead Letter Queues (DLQs)**:
    -   **Kafka DLQ**: Configure Kafka consumers to forward messages that exhaust their retry attempts or encounter non-retryable exceptions to a dedicated DLQ topic (e.g., `original.topic.DLT`).
    -   **DLQ Monitoring**: Implement monitoring and alerting on DLQ topics to notify operators of failed messages.
    -   **Reprocessing**: Provide tools or mechanisms to inspect messages in the DLQ, correct underlying issues, and reprocess them (either manually or automatically).
-   **Exception Handling**:
    -   **Global Exception Handlers**: Implement `@ControllerAdvice` in Spring Boot REST services for consistent API error responses (e.g., using `ProblemDetail` or custom error objects).
    -   **Custom Exceptions**: Define a hierarchy of custom exceptions to categorize different types of failures (e.g., `TransientException`, `BusinessValidationException`, `MalformedMessageException`).
    -   **Structured Logging**: Ensure all exceptions are logged with relevant context (e.g., `correlationId`, `paymentId`, stack trace) in a structured format (e.g., JSON) for easier analysis.

## Alternatives Considered

1.  **No Explicit Retry/DLQ**:
    *   **Pros**: Simplest implementation.
    *   **Cons**: Messages are lost on failure, leading to data inconsistency, manual recovery, and poor system reliability. Unacceptable for a financial system.
2.  **Manual Retry/Recovery Only**:
    *   **Pros**: Avoids some complexity of automated retries.
    *   **Cons**: High operational burden, slow recovery times, and prone to human error. Only suitable for very low-volume, non-critical failures.
3.  **External Workflow Engine for Retries**:
    *   **Pros**: Offloads retry logic to a specialized system.
    *   **Cons**: Adds another complex component to the architecture, potentially over-engineered for the current showcase scope.

## References

-   Spring Kafka Error Handling: https://docs.spring.io/spring-kafka/docs/current/reference/html/#error-handling
-   Spring Retry: https://docs.spring.io/spring-retry/docs/current/reference/html/
-   Resilience4j (for advanced retry, circuit breaker patterns): https://resilience4j.readme.io/
-   Dead Letter Queue Pattern: https://microservices.io/patterns/data/dead-letter-channel.html