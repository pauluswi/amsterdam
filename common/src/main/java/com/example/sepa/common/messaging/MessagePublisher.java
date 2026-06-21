package com.example.sepa.common.messaging;

/**
 * Simple messaging abstraction so we can swap Kafka with an in-memory publisher for showcase mode.
 */
public interface MessagePublisher {
    <T> void send(String topic, String key, T payload);
}