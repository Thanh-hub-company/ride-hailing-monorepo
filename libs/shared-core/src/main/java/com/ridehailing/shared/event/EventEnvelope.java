package com.ridehailing.shared.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventEnvelope<T> {
    private String eventId;
    private String eventType;
    private String eventVersion;
    private Instant occurredAt;
    private String correlationId;
    private String producer;
    private T payload;
}
