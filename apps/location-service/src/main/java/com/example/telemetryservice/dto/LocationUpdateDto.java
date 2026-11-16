package com.example.telemetryservice.dto;

import java.time.Instant;
import java.util.UUID;

public record LocationUpdateDto(
        UUID driverId,
        double latitude,
        double longitude,
        Double headingDegrees,
        Double speedMetersPerSecond,
        Instant timestamp
) {}