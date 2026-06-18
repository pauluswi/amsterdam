package com.example.sepa.common.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {
    private BigDecimal amount;
    private String currency;
    private ServiceLevel serviceLevel;
    private String debtorIban;
    private String creditorIban;
    private String creditorName;
    private String instructionId; // Optional, can be generated if not provided
    private String endToEndId;    // Optional, can be generated if not provided
}
