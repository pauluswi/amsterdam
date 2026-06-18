package com.example.sepa.common.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentStatusEvent extends BaseEvent {
    private String paymentId;
    private String status; // e.g., ACCEPTED, REJECTED
    private String reason; // Optional reason for rejection
    private String mockPacs002; // Mocked ISO 20022 message content (ADR-003)

    public PaymentStatusEvent(String correlationId, String paymentId, String status, String reason, String mockPacs002) {
        super(correlationId);
        this.paymentId = paymentId;
        this.status = status;
        this.reason = reason;
        this.mockPacs002 = mockPacs002;
    }
}