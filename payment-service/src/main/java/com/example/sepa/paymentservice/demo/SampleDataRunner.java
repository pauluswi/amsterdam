package com.example.sepa.paymentservice.demo;

import com.example.sepa.common.ServiceLevel;
import com.example.sepa.paymentservice.dto.PaymentRequestDTO;
import com.example.sepa.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Creates sample payments at startup when "showcase" profile is active.
 */
@Component
@Profile("showcase")
@RequiredArgsConstructor
@Slf4j
public class SampleDataRunner implements CommandLineRunner {

    private final PaymentService paymentService;

    @Override
    public void run(String... args) {
        log.info("Showcase profile active - creating sample payments...");

        createSamplePayment(ServiceLevel.INST, new BigDecimal("12.34"), "EUR", "DE12500105170648489890", "NL39RABO0300065264");
        createSamplePayment(ServiceLevel.SEPA, new BigDecimal("50.00"), "EUR", "FR7630006000011234567890189", "DE89370400440532013000");
        createSamplePayment(ServiceLevel.INST, new BigDecimal("100.99"), "EUR", "ES9121000418450200051332", "IT60X0542811101000000123456");

        log.info("Sample payments created.");
    }

    private void createSamplePayment(ServiceLevel level, BigDecimal amount, String currency, String debtorIban, String creditorIban) {
        PaymentRequestDTO dto = new PaymentRequestDTO();
        dto.setServiceLevel(level);
        dto.setAmount(amount);
        dto.setCurrency(currency);
        dto.setDebtorIban(debtorIban);
        dto.setCreditorIban(creditorIban);

        String idempotencyKey = "showcase-key-" + UUID.randomUUID();
        try {
            paymentService.initiatePayment(dto, idempotencyKey);
            log.info("Sample payment created (idempotencyKey={})", idempotencyKey);
        } catch (Exception e) {
            log.error("Failed to create sample payment", e);
        }
    }
}