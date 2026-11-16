package com.example.commoncontracts.dto;

import java.util.UUID;

public record DriverProfileDto(
        UUID driverId,
        String fullName,
        String phoneNumber,
        String vehicleLicensePlate,
        String status
) {}
