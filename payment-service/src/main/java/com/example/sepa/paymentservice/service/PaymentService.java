package com.example.sepa.paymentservice.service;

import com.example.sepa.common.event.BaseEvent;
import com.example.sepa.common.event.PaymentInitiatedEvent;
import com.example.sepa.paymentservice.dto.PaymentRequestDTO;
import com.example.sepa.paymentservice.entity.Payment;
import com.example.sepa.paymentservice.iso20022.Iso20022Generator;
import com.example.sepa.paymentservice.repository.PaymentRepository;
import com.example.sepa.paymentservice.filter.CorrelationIdFilter; // Import for MDC key
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final Iso20022Generator iso20022Generator;
    private final KafkaTemplate<String, BaseEvent> kafkaTemplate;

    @Value("${app.kafka.topics.payment-initiated}")
    private String paymentInitiatedTopic;

    @Transactional
    public Payment initiatePayment(PaymentRequestDTO requestDTO, String idempotencyKey) {
        String paymentId = UUID.randomUUID().toString();
        String correlationId = MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);

        log.info("Initiating payment with ID: {} and Correlation ID: {}", paymentId, correlationId);

        // 1. Create Payment entity
        Payment payment = Payment.builder()
                .id(paymentId)
                .serviceLevel(requestDTO.getServiceLevel())
                .amount(requestDTO.getAmount())
                .currency(requestDTO.getCurrency())
                .debtorIban(requestDTO.getDebtorIban())
                .creditorIban(requestDTO.getCreditorIban())
                .status("INITIATED") // Initial status
                .createdAt(Instant.now())
                .lastUpdatedAt(Instant.now())
                .correlationId(correlationId)
                .idempotencyKey(idempotencyKey)
                .build();

        // 2. Save Payment to database
        paymentRepository.save(payment);
        log.debug("Payment {} saved to database.", paymentId);

        // 3. Generate mock ISO 20022 pacs.008 message (ADR-003)
        String mockPacs008 = iso20022Generator.generatePacs008(payment);
        log.debug("Mock pacs.008 generated for payment {}.", paymentId);

        // 4. Publish PaymentInitiatedEvent to Kafka (ADR-001)
        PaymentInitiatedEvent event = new PaymentInitiatedEvent(
                correlationId,
                payment.getId(),
                payment.getServiceLevel(),
                payment.getAmount().toPlainString(),
                payment.getCurrency(),
                payment.getDebtorIban(),
                payment.getCreditorIban(),
                mockPacs008
        );

        kafkaTemplate.send(paymentInitiatedTopic, payment.getId(), event);
        log.info("PaymentInitiatedEvent for payment {} published to Kafka topic {}.", paymentId, paymentInitiatedTopic);

        return payment;
    }
}