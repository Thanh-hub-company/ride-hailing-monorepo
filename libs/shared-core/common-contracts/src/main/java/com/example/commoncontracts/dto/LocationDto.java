package com.example.commoncontracts.dto;

import java.time.Instant;

public record LocationDto(
        double latitude,
        double longitude,
        Double accuracyMeters,
        Double headingDegrees,
        Double speedMetersPerSecond,
        Instant deviceTimestamp
) {}