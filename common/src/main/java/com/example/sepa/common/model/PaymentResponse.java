package com.example.sepa.common.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponse {
    private String paymentId;
    private String correlationId;
    private PaymentStatus status;
    private String message;
}
