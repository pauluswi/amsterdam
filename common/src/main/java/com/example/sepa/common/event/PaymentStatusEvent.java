package com.example.sepa.common.event;

import com.example.sepa.common.ServiceLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Enriched PaymentStatusEvent carries payment identifiers and minimal bookkeeping data
 * so consumers (payment-service, ledger-service) have the information they need
 * without querying other services.
 *
 * Backwards-compatible constructor retained for existing code.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentStatusEvent extends BaseEvent {
    private String paymentId;
    private String status; // e.g., ACCEPTED, REJECTED
    private String reason; // Optional reason for rejection
    private String mockPacs002; // Mocked ISO 20022 message content (ADR-003)

    // Enrichment fields (recommended)
    private String amount;      // string representation of amount (consistent with PaymentInitiatedEvent)
    private String currency;
    private String debtorIban;
    private String creditorIban;
    private ServiceLevel serviceLevel;

    // Backwards-compatible constructor (existing code uses this)
    public PaymentStatusEvent(String correlationId, String paymentId, String status, String reason, String mockPacs002) {
        super(correlationId);
        this.paymentId = paymentId;
        this.status = status;
        this.reason = reason;
        this.mockPacs002 = mockPacs002;
    }

    // New richer constructor (preferred)
    public PaymentStatusEvent(String correlationId,
                              String paymentId,
                              String status,
                              String reason,
                              String mockPacs002,
                              String amount,
                              String currency,
                              String debtorIban,
                              String creditorIban,
                              ServiceLevel serviceLevel) {
        super(correlationId);
        this.paymentId = paymentId;
        this.status = status;
        this.reason = reason;
        this.mockPacs002 = mockPacs002;
        this.amount = amount;
        this.currency = currency;
        this.debtorIban = debtorIban;
        this.creditorIban = creditorIban;
        this.serviceLevel = serviceLevel;
    }
}