package com.example.tripservice.exception;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        String code,
        String message,
        String correlationId,
        Map<String, Object> details,
        Instant timestamp
) {
}
