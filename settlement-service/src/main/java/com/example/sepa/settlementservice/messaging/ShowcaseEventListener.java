package com.example.sepa.settlementservice.messaging;

import com.example.sepa.common.messaging.InMemoryMessage;
import com.example.sepa.common.event.PaymentInitiatedEvent;
import com.example.sepa.settlementservice.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Listener that receives in-memory messages (published by ShowcaseMessagePublisher) and
 * routes payment.initiated messages into SettlementService. Active in 'showcase' profile.
 */
@Component
@Profile("showcase")
@RequiredArgsConstructor
@Slf4j
public class ShowcaseEventListener {

    private final SettlementService settlementService;

    @EventListener
    public void onInMemoryMessage(InMemoryMessage msg) {
        if (msg == null) return;
        if ("payment.initiated".equals(msg.getTopic())) {
            Object payload = msg.getPayload();
            if (payload instanceof PaymentInitiatedEvent) {
                log.debug("ShowcaseEventListener routing payment.initiated to SettlementService for paymentId={}", ((PaymentInitiatedEvent) payload).getPaymentId());
                // delegate to existing listener method; pass null for correlation header
                settlementService.listenPaymentInitiated((PaymentInitiatedEvent) payload, null);
            } else {
                log.warn("ShowcaseEventListener received non-PaymentInitiatedEvent payload: {}", payload);
            }
        }
    }
}