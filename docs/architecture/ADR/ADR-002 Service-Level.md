# ADR-002: Service-Level Routing for SEPA Credit Transfer vs. SEPA Instant Credit Transfer

## Status
Accepted

## Context

The showcase must support two distinct SEPA payment types with different settlement characteristics:
- **SEPA Credit Transfer (SCT)**: Standard 1-day clearing cycle
- **SEPA Instant Credit Transfer (SCT Inst)**: Real-time settlement (seconds)

Both use identical ISO 20022 `pacs.008` initiation messages but differ in:
- Settlement timing and SLA
- Status response expectations
- Return processing windows
- Service level code in the payment message

Without explicit routing logic, the settlement flow would be ambiguous and error-prone.

## Decision

Add a `serviceLevel` field to the payment domain model with values `SEPA` or `INST`.

The Settlement Service routes payment events to service-level-specific handlers based on this field.

Both handlers produce `pacs.008` messages but with appropriate service level codes and settlement timing configurations.

## Rationale

1.  **Explicit Intent**: Payment originator clearly specifies desired settlement speed
2.  **Correct Timing**: Settlement simulator respects T+1 for SCT, real-time for SCT Inst
3.  **Compliance**: Service level code in ISO 20022 message matches actual processing
4.  **Extensibility**: Easy to add new service levels (e.g., SEPA B2B) in future
5.  **Clear Separation**: Different business rules per service level don't mix in same code path

## Consequences

### Positive
- Clear, explicit routing eliminates ambiguity
- Each service level can have independent SLA monitoring and alerting
- Ledger and reporting properly reflect payment classification
- Return processing can enforce correct windows per service level

### Negative
- Settlement Service has two code paths (slight complexity increase)
- Testing must cover both paths
- Configuration must define settlement timing per service level
- Potential for misconfiguration if service level codes don't match ISO 20022 standard

## Implementation Notes

### Domain Model Update

```java
public enum ServiceLevel {
    SEPA,    // Standard 1-day clearing
    INST     // Instant, real-time
}

public class Payment {
    private String paymentId;
    private ServiceLevel serviceLevel;  // New field
    // ... other fields
}
```

### Settlement Service Routing

-   Topic: `payment.initiated` contains `serviceLevel` field
-   Router checks `serviceLevel` and delegates to appropriate handler
    -   `SepaSettlementHandler`: T+1 day settlement window
    -   `InstSettlementHandler`: Real-time settlement (0-30 seconds)

### ISO 20022 Mapping

-   SCT: `ChrgBr` (charge bearer) = `SLEV`, `SvcLvl/Cd` = `SEPA`
-   SCT Inst: `ChrgBr` = `SLEV`, `SvcLvl/Cd` = `INST`

### Configuration (`application.yml`)

```yaml
settlement:
  service-levels:
    SEPA:
      settlement-window-days: 1
      retry-attempts: 3
    INST:
      settlement-window-seconds: 30
      retry-attempts: 5
```

## Alternatives Considered

-   Single generic handler with timing parameter: Less explicit; harder to enforce different rules
-   Separate microservices per service level: Over-engineered; excessive operational overhead
-   Detect service level from message content: Fragile; requires parsing before routing

## References

-   EPC SEPA Credit Transfer Rulebook: https://www.europeanpaymentscouncil.eu/
-   EPC SEPA Instant Credit Transfer Rulebook: https://www.europeanpaymentscouncil.eu/
-   ISO 20022 pacs.008: https://www.iso20022.org/
