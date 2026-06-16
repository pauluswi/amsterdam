# ADR-005: Use PostgreSQL as System of Record

## Status
Accepted

## Context

The showcase application requires a robust and reliable data store for critical business information, including:
-   Payment details and their lifecycle status.
-   Ledger entries for debit and credit transactions, which demand high data integrity.
-   Audit records of all significant events for compliance and traceability.

The `arc42.md` document explicitly identifies PostgreSQL as the chosen database technology. A clear architectural decision is needed to formalize this choice and outline its implications.

## Decision

PostgreSQL will be used as the primary system of record for all persistent data within the showcase application, including payment, ledger, and audit records.

## Rationale

1.  **Strong Transactional Consistency (ACID)**: PostgreSQL is a fully ACID-compliant relational database, which is crucial for financial applications where data integrity and consistency are paramount, especially for ledger operations.
2.  **Suitability for Ledger Data**: Its robust support for transactions, foreign keys, and indexing makes it an excellent choice for managing financial ledgers and ensuring accurate accounting.
3.  **Maturity and Reliability**: PostgreSQL is a mature, open-source database with a proven track record of stability and reliability in production environments.
4.  **Rich Feature Set**: It offers advanced features like JSONB support, extensibility, and a powerful query optimizer, which can be beneficial for various data access patterns.
5.  **Ease of Local Setup**: For the showcase, PostgreSQL is easily deployable via Docker, aligning with the project's local development and demonstration strategy.
6.  **Community Support**: A large and active community provides extensive documentation, tools, and support.

## Consequences

### Positive
-   Guaranteed data consistency for financial transactions.
-   Simplified data modeling due to the relational nature.
-   Leverages a widely understood and supported database technology.
-   Good foundation for future reporting and analytical needs.

### Negative
-   Requires careful design of transaction boundaries to avoid contention and ensure performance.
-   While suitable for the showcase, high-volume production environments would necessitate advanced scaling strategies (e.g., sharding, replication, partitioning) and performance tuning.
-   Schema migrations need to be managed carefully.
-   Potential for impedance mismatch between object-oriented application code and relational database schema.

## Implementation Notes

-   Utilize Spring Data JPA or a similar ORM framework for database interaction to abstract away raw SQL.
-   Define clear transaction boundaries using `@Transactional` annotations or programmatic transaction management.
-   Implement proper indexing on frequently queried columns (e.g., `paymentId`, `accountId`, timestamps).
-   Consider using database-level constraints (e.g., `NOT NULL`, `UNIQUE`, `CHECK`) to enforce data integrity where appropriate.
-   For audit trails, consider immutable tables or append-only patterns to prevent accidental modification of historical data.

## Alternatives Considered

1.  **NoSQL Databases (e.g., MongoDB, Cassandra)**:
    *   **Pros**: High scalability, flexible schema.
    *   **Cons**: Typically offer eventual consistency, which is generally not acceptable for core financial ledger data where strong consistency is a hard requirement. Less mature transactional support.
2.  **In-memory Databases (e.g., H2 for testing)**:
    *   **Pros**: Very fast for testing.
    *   **Cons**: Not suitable for persistent production data; primarily used for development and testing.
3.  **Other Relational Databases (e.g., MySQL, Oracle)**:
    *   **Pros**: Similar benefits to PostgreSQL.
    *   **Cons**: PostgreSQL was chosen for its strong feature set, open-source nature, and community, fitting the project's context well without significant drawbacks compared to these alternatives for the showcase scope.

## References

-   PostgreSQL Official Website: https://www.postgresql.org/
-   ACID properties: https://en.wikipedia.org/wiki/ACID