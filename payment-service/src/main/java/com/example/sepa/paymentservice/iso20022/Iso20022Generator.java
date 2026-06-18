package com.example.sepa.paymentservice.iso20022;

import com.example.sepa.paymentservice.entity.Payment;

public interface Iso20022Generator {
    String generatePacs008(Payment payment);
    // Add other ISO 20022 message types as needed (e.g., pacs.002, pacs.004)
}