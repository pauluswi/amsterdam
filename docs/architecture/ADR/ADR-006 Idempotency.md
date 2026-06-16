# ADR-006: Idempotency for Payment Requests

## Status
Accepted

## Context

In a distributed system, especially one handling financial transactions, network issues, client-side retries, or system failures can lead to the same request being sent multiple times. Without a mechanism to handle these duplicate requests, the system could process the same payment instruction more than once, leading to incorrect ledger entries and significant financial discrepancies.

The `arc42.md` document explicitly lists "Idempotency" as a cross-cutting concept and includes "QR-01: Duplicate payment submitted with same idempotency key" as a quality requirement, emphasizing its importance for the showcase.

## Decision

All payment initiation API endpoints will be designed to be idempotent. Clients will be required to provide a unique `Idempotency-Key` header with each payment request. The system will use this key to ensure that a request is processed only once, even if it is received multiple times.

## Rationale

1.  **Prevent Duplicate Transactions**: The primary goal is to avoid processing the same payment instruction multiple times, which is critical for maintaining data integrity in a financial system.
2.  **Safe Retries**: Allows clients to safely retry requests without fear of unintended side effects, improving the robustness of client-server interactions, especially in the face of network transient errors.
3.  **Data Consistency**: Ensures that the system's state (e.g., ledger balances, payment statuses) remains consistent, even when external factors cause request duplication.
4.  **Improved User Experience**: Clients can implement simpler retry logic, leading to a more reliable and predictable experience.
5.  **Compliance**: Adheres to best practices for financial API design, where idempotency is a common requirement.

## Consequences

### Positive
-   Eliminates the risk of duplicate payment processing.
-   Enhances the reliability and fault tolerance of the payment API.
-   Simplifies client-side error handling and retry logic.
-   Contributes to a more robust and trustworthy financial system.

### Negative
-   Requires clients to generate and manage unique `Idempotency-Key` values for each request.
-   Adds complexity to the server-side implementation, as the system must store and check idempotency keys.
-   Increased storage requirements for idempotency keys and their associated responses.
-   Potential for race conditions if the check-and-store operation for idempotency keys is not atomic.
-   Requires careful consideration of the lifespan and cleanup of stored idempotency keys.

## Implementation Notes

-   **Client Responsibility**: Clients must generate a unique, non-reusable `Idempotency-Key` (e.g., a UUID) for each distinct payment initiation request and include it in the `Idempotency-Key` HTTP header.
-   **Server-Side Logic**:
    1.  Upon receiving a request with an `Idempotency-Key`, the server will first check if this key has been seen before and if a response has already been stored for it.
    2.  If a stored response is found, the server will immediately return that response without re-processing the request.
    3.  If the key is new, the server will process the request. Before returning the result, it will store the `Idempotency-Key` along with the request parameters and the final response.
    4.  The storage of the `Idempotency-Key` and the processing of the request must be atomic to prevent race conditions where two identical requests arrive simultaneously. This can be achieved using database transactions or distributed locks.
-   **Storage**: A dedicated store (e.g., a table in PostgreSQL or a Redis cache) will be used to store idempotency keys and their corresponding responses.
-   **Key Lifespan**: Implement a mechanism to expire or clean up old idempotency keys to manage storage space (e.g., after 24 hours, or once the payment has reached a final state).
-   **Error Handling**: Ensure that even if the payment processing fails, the `Idempotency-Key` is still recorded with the error response, so subsequent retries with the same key return the same error.

## Alternatives Considered

1.  **No Idempotency**:
    *   **Pros**: Simplest implementation.
    *   **Cons**: High risk of duplicate transactions, data inconsistencies, and poor system reliability, unacceptable for financial systems.
2.  **Application-Specific Unique Identifiers**:
    *   **Pros**: Can achieve similar deduplication.
    *   **Cons**: Often tied to specific business logic (e.g., `paymentId`), which might not be available at the very beginning of the request processing, or might not cover all scenarios where a request could be duplicated before a business ID is assigned. The `Idempotency-Key` header is a more generic and widely adopted pattern for API-level idempotency.
3.  **Client-Side Deduplication Only**:
    *   **Pros**: Reduces server-side complexity.
    *   **Cons**: Unreliable, as clients cannot guarantee that a request was not processed by the server before a network error occurred.

## References

-   Stripe API Idempotency Guide: https://stripe.com/docs/api/idempotent_requests
-   RFC 7231, Section 4.2.2: Idempotent Methods (HTTP standard for idempotency): https://datatracker.ietf.org/doc/html/rfc7231#section-4.2.2