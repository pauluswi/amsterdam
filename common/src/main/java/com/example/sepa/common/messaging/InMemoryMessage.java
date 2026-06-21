package com.example.sepa.common.messaging;

/**
 * In-memory message wrapper published as a Spring application event in showcase mode.
 */
public class InMemoryMessage {
    private final String topic;
    private final String key;
    private final Object payload;

    public InMemoryMessage(String topic, String key, Object payload) {
        this.topic = topic;
        this.key = key;
        this.payload = payload;
    }

    public String getTopic() {
        return topic;
    }

    public String getKey() {
        return key;
    }

    public Object getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "InMemoryMessage{" +
                "topic='" + topic + '\'' +
                ", key='" + key + '\'' +
                ", payload=" + payload +
                '}';
    }
}