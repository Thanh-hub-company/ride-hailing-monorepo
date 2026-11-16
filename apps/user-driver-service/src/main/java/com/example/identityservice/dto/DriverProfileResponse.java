package com.example.identityservice.dto;

import com.example.commoncontracts.enums.DriverStatus;

import java.util.UUID;

public record DriverProfileResponse(
        UUID id,
        UUID userId,
        String fullName,
        String phoneNumber,
        String vehicleLicensePlate,
        String vehicleModel,
        DriverStatus status,
        Double rating,
        Boolean isApproved
) {}
