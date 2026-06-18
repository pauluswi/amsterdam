package com.example.sepa.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiatedEvent implements Serializable {
    private String paymentId;
    private String correlationId;
    private String instructionId; // From ISO 20022 pacs.008
    private String endToEndId;    // From ISO 20022 pacs.008
    private BigDecimal amount;
    private String currency;
    private ServiceLevel serviceLevel;
    private String debtorIban;
    private String creditorIban;
    private String creditorName;
    private String pacs008Message; // Mocked ISO 20022 pacs.008 XML content
    private LocalDateTime initiatedAt;
}
