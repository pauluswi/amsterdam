package com.example.sepa.paymentservice.service;

import com.example.sepa.common.event.PaymentStatusEvent;
import com.example.sepa.paymentservice.entity.Payment;
import com.example.sepa.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentStatusConsumer {

    private final PaymentRepository paymentRepository;

    @KafkaListener(topics = "${app.kafka.topics.payment-status}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void handlePaymentStatus(PaymentStatusEvent event) {
        log.info("Received PaymentStatusEvent for paymentId={} status={}", event.getPaymentId(), event.getStatus());

        paymentRepository.findById(event.getPaymentId()).ifPresentOrElse(payment -> {
            payment.setStatus(event.getStatus());
            payment.setLastUpdatedAt(Instant.now());
            paymentRepository.save(payment);
            log.info("Updated payment {} status -> {}", payment.getId(), payment.getStatus());
        }, () -> {
            log.warn("Payment with id {} not found when processing PaymentStatusEvent", event.getPaymentId());
        });
    }
}