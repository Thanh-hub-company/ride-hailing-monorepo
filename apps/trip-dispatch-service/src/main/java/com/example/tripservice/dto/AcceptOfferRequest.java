package com.example.tripservice.dto;

import java.util.UUID;

public record AcceptOfferRequest(
        UUID offerId
) {
}
