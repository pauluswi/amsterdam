package com.example.sepa.settlementservice.messaging;

import com.example.sepa.common.event.BaseEvent;
import com.example.sepa.common.messaging.MessagePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Production Kafka publisher - active when not running with 'showcase' profile.
 */
@Component
@Profile("!showcase")
@RequiredArgsConstructor
public class KafkaMessagePublisher implements MessagePublisher {

    private final KafkaTemplate<String, BaseEvent> kafkaTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public <T> void send(String topic, String key, T payload) {
        // Cast to BaseEvent (existing code uses BaseEvent)
        kafkaTemplate.send(topic, key, (BaseEvent) payload);
    }
}