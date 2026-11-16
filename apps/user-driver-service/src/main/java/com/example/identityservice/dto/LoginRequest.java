package com.example.identityservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record LoginRequest(
        @JsonAlias({"username", "user_name", "account", "login"})
        String identifier,

        @JsonAlias({"email_address", "mail"})
        String email,

        @JsonAlias({"phone_number", "mobile", "cellphone"})
        String phoneNumber,

        String phone,

        @JsonAlias({"pass", "pwd", "passwd"})
        String password
) {}