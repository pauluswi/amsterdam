package com.example.sepa.settlementservice.service;

import com.example.sepa.common.ServiceLevel;
import com.example.sepa.common.event.BaseEvent;
import com.example.sepa.common.event.PaymentInitiatedEvent;
import com.example.sepa.common.event.PaymentStatusEvent;
import com.example.sepa.settlementservice.iso20022.Iso20022Generator;
import com.example.sepa.settlementservice.filter.CorrelationIdFilter; // Import for MDC key
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementService {

    private final Iso20022Generator iso20022Generator;
    private final KafkaTemplate<String, BaseEvent> kafkaTemplate;
    private final Random random = new Random();

    @Value("${app.kafka.topics.payment-status}")
    private String paymentStatusTopic;

    @KafkaListener(topics = "${app.kafka.topics.payment-initiated}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenPaymentInitiated(@Payload PaymentInitiatedEvent event,
                                       @Header(name = CorrelationIdFilter.CORRELATION_ID_HEADER, required = false) String correlationIdHeader) {

        // Set correlationId in MDC for consistent logging (ADR-008)
        if (correlationIdHeader != null && !correlationIdHeader.isEmpty()) {
            MDC.put(CorrelationIdFilter.CORRELATION_ID_MDC_KEY, correlationIdHeader);
        } else {
            // Fallback if header is not present, use event's correlationId
            MDC.put(CorrelationIdFilter.CORRELATION_ID_MDC_KEY, event.getCorrelationId());
        }

        log.info("Received PaymentInitiatedEvent for payment ID: {} (Service Level: {})", event.getPaymentId(), event.getServiceLevel());

        String status;
        String reason = null;
        String mockPacs002;

        try {
            // Simulate settlement based on service level (ADR-002)
            switch (event.getServiceLevel()) {
                case SEPA:
                    log.info("Simulating SEPA Credit Transfer (T+1) for payment ID: {}", event.getPaymentId());
                    TimeUnit.SECONDS.sleep(random.nextInt(3) + 1); // Simulate 1-3 seconds delay
                    break;
                case INST:
                    log.info("Simulating SEPA Instant Credit Transfer (real-time) for payment ID: {}", event.getPaymentId());
                    TimeUnit.MILLISECONDS.sleep(random.nextInt(500) + 100); // Simulate 100-500 ms delay
                    break;
                default:
                    log.warn("Unknown ServiceLevel: {} for payment ID: {}", event.getServiceLevel(), event.getPaymentId());
                    throw new IllegalArgumentException("Unknown ServiceLevel: " + event.getServiceLevel());
            }

            // Mock decision for ACCEPTED/REJECTED
            if (random.nextBoolean()) { // 50% chance of acceptance
                status = "ACCEPTED";
                log.info("Payment ID: {} settlement simulated as ACCEPTED.", event.getPaymentId());
            } else {
                status = "REJECTED";
                reason = "FUNDS_INSUFFICIENT"; // Mock reason
                log.warn("Payment ID: {} settlement simulated as REJECTED. Reason: {}", event.getPaymentId(), reason);
            }

            // Generate mock pacs.002 (ADR-003)
            mockPacs002 = iso20022Generator.generatePacs002(event, status, reason);
            log.debug("Mock pacs.002 generated for payment {}.", event.getPaymentId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Settlement simulation interrupted for payment ID: {}", event.getPaymentId(), e);
            status = "FAILED";
            reason = "SIMULATION_INTERRUPTED";
            mockPacs002 = iso20022Generator.generatePacs002(event, status, reason);
        } catch (Exception e) {
            log.error("Error during settlement simulation for payment ID: {}", event.getPaymentId(), e);
            status = "FAILED";
            reason = "INTERNAL_ERROR";
            mockPacs002 = iso20022Generator.generatePacs002(event, status, reason);
        } finally {
            MDC.remove(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
        }

        // Publish PaymentStatusEvent to Kafka
        PaymentStatusEvent statusEvent = new PaymentStatusEvent(
                event.getCorrelationId(),
                event.getPaymentId(),
                status,
                reason,
                mockPacs002
        );
        kafkaTemplate.send(paymentStatusTopic, event.getPaymentId(), statusEvent);
        log.info("PaymentStatusEvent for payment ID: {} (Status: {}) published to Kafka topic {}.", event.getPaymentId(), status, paymentStatusTopic);
    }
}