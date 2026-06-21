package com.example.sepa.settlementservice.controller;

import com.example.sepa.common.event.PaymentInitiatedEvent;
import com.example.sepa.common.event.PaymentStatusEvent;
import com.example.sepa.settlementservice.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * Simple controller to trigger settlement processing synchronously.
 * Uses existing SettlementService logic so behavior is consistent.
 */
@RestController
@RequestMapping("/settlements")
@RequiredArgsConstructor
@Slf4j
public class SettlementController {

    private final SettlementService settlementService;

    @PostMapping("/process")
    public PaymentStatusEvent processNow(@RequestBody PaymentInitiatedEvent event,
                                         @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        log.info("SettlementController.processNow called for paymentId={}", event.getPaymentId());
        // Existing SettlementService has a method listenPaymentInitiated(PaymentInitiatedEvent, String)
        // We call it directly and it will publish the PaymentStatusEvent downstream.
        settlementService.listenPaymentInitiated(event, correlationId);
        // For a synchronous return you could modify settlementService to return the PaymentStatusEvent.
        return null;
    }
}