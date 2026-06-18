package com.example.sepa.paymentservice.controller;

import com.example.sepa.paymentservice.dto.PaymentRequestDTO;
import com.example.sepa.paymentservice.entity.Payment;
import com.example.sepa.paymentservice.filter.CorrelationIdFilter;
import com.example.sepa.paymentservice.filter.IdempotencyFilter;
import com.example.sepa.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Payment> initiatePayment(
            @Valid @RequestBody PaymentRequestDTO requestDTO,
            @RequestHeader(name = IdempotencyFilter.IDEMPOTENCY_KEY_HEADER, required = true) String idempotencyKey) {

        String correlationId = MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
        log.info("Received payment initiation request with Correlation ID: {} and Idempotency Key: {}", correlationId, idempotencyKey);

        // The IdempotencyFilter already handles returning stored responses for duplicate keys.
        // If we reach here, it's either a new request or a request that needs to be processed.
        Payment initiatedPayment = paymentService.initiatePayment(requestDTO, idempotencyKey);

        log.info("Payment initiated successfully with ID: {} for Correlation ID: {}", initiatedPayment.getId(), correlationId);
        return new ResponseEntity<>(initiatedPayment, HttpStatus.ACCEPTED);
    }
}