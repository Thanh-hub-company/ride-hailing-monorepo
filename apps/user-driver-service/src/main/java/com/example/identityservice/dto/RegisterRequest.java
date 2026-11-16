package com.example.identityservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record RegisterRequest(
        @JsonAlias({"email_address", "mail"})
        String email,

        @JsonAlias({"phone_number", "mobile", "cellphone"})
        String phoneNumber,

        String phone,

        @JsonAlias({"pass", "pwd", "passwd"})
        String password,

        @JsonAlias({"name", "full_name"})
        String fullName,

        String role, // CUSTOMER hoặc DRIVER

        @JsonAlias({"vehicle_license_plate", "license_plate", "plate"})
        String vehicleLicensePlate, // Dành riêng cho DRIVER

        @JsonAlias({"vehicle_model", "model"})
        String vehicleModel        // Dành riêng cho DRIVER
) {}