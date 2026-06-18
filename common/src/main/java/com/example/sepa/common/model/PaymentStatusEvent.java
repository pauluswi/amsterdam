package com.example.sepa.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusEvent implements Serializable {
    private String paymentId;
    private String correlationId;
    private PaymentStatus status;
    private String reason; // e.g., "Insufficient funds", "Accepted"
    private String pacs002Message; // Mocked ISO 20022 pacs.002 XML content
    private LocalDateTime timestamp;
}
