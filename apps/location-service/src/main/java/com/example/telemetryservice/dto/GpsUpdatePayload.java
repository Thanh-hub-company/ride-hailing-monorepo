package com.example.telemetryservice.dto;

import java.util.UUID;

public record GpsUpdatePayload(
        UUID driverId,
        double latitude,
        double longitude,
        Double heading,
        Double speed
) {}
