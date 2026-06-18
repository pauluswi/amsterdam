package com.example.sepa.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent {
    private String correlationId;
    private Instant timestamp;

    public BaseEvent(String correlationId) {
        this.correlationId = correlationId;
        this.timestamp = Instant.now();
    }
}
