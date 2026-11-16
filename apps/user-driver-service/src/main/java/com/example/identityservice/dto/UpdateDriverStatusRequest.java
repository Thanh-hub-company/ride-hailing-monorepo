package com.example.identityservice.dto;

import com.example.commoncontracts.enums.DriverStatus;

public record UpdateDriverStatusRequest(
        DriverStatus status
) {}