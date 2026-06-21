package com.example.sepa.paymentservice.messaging;

import com.example.sepa.common.messaging.InMemoryMessage;
import com.example.sepa.common.event.PaymentStatusEvent;
import com.example.sepa.paymentservice.service.PaymentStatusConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * In showcase mode, listen for in-memory messages and route payment.status messages
 * to the existing PaymentStatusConsumer.
 */
@Component
@Profile("showcase")
@RequiredArgsConstructor
@Slf4j
public class ShowcaseEventListener {

    private final PaymentStatusConsumer paymentStatusConsumer;

    @EventListener
    public void onInMemoryMessage(InMemoryMessage msg) {
        if (msg == null) return;
        if ("payment.status".equals(msg.getTopic())) {
            Object payload = msg.getPayload();
            if (payload instanceof PaymentStatusEvent) {
                log.debug("ShowcaseEventListener routing payment.status to PaymentStatusConsumer for paymentId={}", ((PaymentStatusEvent) payload).getPaymentId());
                paymentStatusConsumer.handlePaymentStatus((PaymentStatusEvent) payload);
            } else {
                log.warn("ShowcaseEventListener received non-PaymentStatusEvent payload: {}", payload);
            }
        }
    }
}