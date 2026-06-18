package com.example.sepa.common.event;

import com.example.sepa.common.ServiceLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentInitiatedEvent extends BaseEvent {
    private String paymentId;
    private ServiceLevel serviceLevel;
    private String amount; // Using String for currency amount to avoid precision issues
    private String currency;
    private String debtorIban;
    private String creditorIban;
    private String mockPacs008; // Mocked ISO 20022 message content (ADR-003)

    public PaymentInitiatedEvent(String correlationId, String paymentId, ServiceLevel serviceLevel,
                                 String amount, String currency, String debtorIban, String creditorIban,
                                 String mockPacs008) {
        super(correlationId);
        this.paymentId = paymentId;
        this.serviceLevel = serviceLevel;
        this.amount = amount;
        this.currency = currency;
        this.debtorIban = debtorIban;
        this.creditorIban = creditorIban;
        this.mockPacs008 = mockPacs008;
    }
}
