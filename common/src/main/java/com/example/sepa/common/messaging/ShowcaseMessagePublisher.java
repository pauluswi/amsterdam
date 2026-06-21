package com.example.sepa.common.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Showcase (in-memory) message publisher. Active when Spring profile 'showcase' is enabled.
 */
@Component
@Profile("showcase")
@RequiredArgsConstructor
@Slf4j
public class ShowcaseMessagePublisher implements MessagePublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public <T> void send(String topic, String key, T payload) {
        InMemoryMessage msg = new InMemoryMessage(topic, key, payload);
        log.debug("ShowcaseMessagePublisher publishing in-memory message: {}", msg);
        applicationEventPublisher.publishEvent(msg);
    }
}