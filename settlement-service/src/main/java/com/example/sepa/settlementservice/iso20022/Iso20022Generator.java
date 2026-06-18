package com.example.sepa.settlementservice.iso20022;

import com.example.sepa.common.event.PaymentInitiatedEvent;

public interface Iso20022Generator {
    String generatePacs002(PaymentInitiatedEvent paymentEvent, String status, String reason);
    // Add other ISO 20022 message types as needed (e.g., pacs.004)
}