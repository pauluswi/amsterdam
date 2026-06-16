# ADR-003: Generate ISO 20022 XML from Internal Domain Model

## Status
Accepted

## Context

The system is designed to process and generate ISO 20022 XML messages (e.g., `pacs.008`, `pacs.002`, `camt.052`, `camt.053`, `camt.054`) for various payment and reporting flows. Directly using the complex and verbose ISO 20022 XML schema as the internal domain model for business logic can lead to:
- Tight coupling between business logic and external message formats.
- Increased complexity in domain objects, making them harder to understand and maintain.
- Difficulty in evolving the internal system independently of ISO 20022 standard changes or supporting different versions.
- Reduced flexibility if other message standards need to be integrated in the future.

## Decision

The system will maintain a clean, internal domain model that represents the core business concepts (e.g., `Payment`, `AccountEntry`, `Report`). A dedicated ISO 20022 mapping and generation layer will be responsible for translating between this internal domain model and the external ISO 20022 XML messages.

## Rationale

1.  **Decoupling**: Separates the concerns of business logic from external message serialization/deserialization, allowing each to evolve independently.
2.  **Maintainability**: Internal domain models are typically simpler, more object-oriented, and easier to work with for implementing business rules.
3.  **Flexibility and Extensibility**: Facilitates easier adaptation to new versions of ISO 20022 or integration with entirely different message standards (e.g., SWIFT MT) without impacting core business logic.
4.  **Testability**: The mapping logic can be tested in isolation, ensuring correct translation without involving complex business scenarios.
5.  **Clarity**: Prevents the internal system from being cluttered with XML-specific annotations or constructs, leading to a cleaner codebase.

## Consequences

### Positive
-   Cleaner, more focused business logic within the domain services.
-   Easier to understand and maintain the core payment and reporting processes.
-   Improved resilience to changes in external message formats.
-   Better separation of concerns, leading to more modular and testable code.

### Negative
-   Requires an additional layer of code for mapping, increasing initial development effort.
-   Potential for mapping errors if not thoroughly tested.
-   Slight performance overhead due to the transformation process (expected to be negligible for this showcase).
-   Need to manage two distinct models (internal and external ISO 20022) and their synchronization.

## Implementation Notes

-   A dedicated module or component (e.g., `iso20022-mapper` or `Iso20022GeneratorService`) will encapsulate all mapping logic.
-   Utilize libraries for XML marshalling/unmarshalling (e.g., JAXB, Spring OXM, or Jackson XML) to handle the technical aspects of XML generation and parsing.
-   Implement clear interfaces or builder patterns for constructing ISO 20022 messages from internal domain objects.
-   Ensure robust validation (e.g., XSD validation) for both incoming and outgoing ISO 20022 XML messages.

## Alternatives Considered

1.  **Directly use ISO 20022 XML objects as the internal domain model**:
    *   **Pros**: No explicit mapping layer needed.
    *   **Cons**: Leads to highly coupled, verbose, and complex internal models; difficult to evolve; business logic becomes intertwined with XML structure.
2.  **Use a generic data structure (e.g., `Map<String, Object>`) for the internal model**:
    *   **Pros**: Highly flexible.
    *   **Cons**: Lacks type safety; harder to enforce domain constraints; prone to runtime errors; difficult to refactor.

## References

-   ISO 20022 Message Definitions: https://www.iso20022.org/
-   (Optional) Relevant documentation for chosen XML mapping library (e.g., JAXB documentation).