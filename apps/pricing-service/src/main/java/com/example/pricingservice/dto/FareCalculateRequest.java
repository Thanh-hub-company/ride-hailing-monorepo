package com.example.pricingservice.dto;

import com.example.commoncontracts.enums.ServiceType;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FareCalculateRequest(
        ServiceType serviceType,
        Double distanceKm,
        Double estimatedDurationMinutes,
        Double pickupLatitude,
        Double pickupLongitude,
        Double dropoffLatitude,
        Double dropoffLongitude,
        LocationPoint pickup,
        LocationPoint destination,
        @JsonAlias({"pickupAddress", "pickup_address"})
        String pickupAddress,
        @JsonAlias({"destinationAddress", "destination_address", "dropoffAddress", "dropoff_address"})
        String destinationAddress
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LocationPoint(
            Double latitude,
            Double longitude,
            Double lat,
            Double lng,
            String address
    ) {
        public Double getEffectiveLat() {
            return latitude != null ? latitude : lat;
        }

        public Double getEffectiveLng() {
            return longitude != null ? longitude : lng;
        }
    }

    public double getEffectivePickupLat() {
        if (pickupLatitude != null) return pickupLatitude;
        if (pickup != null && pickup.getEffectiveLat() != null) return pickup.getEffectiveLat();
        return 0.0;
    }

    public double getEffectivePickupLng() {
        if (pickupLongitude != null) return pickupLongitude;
        if (pickup != null && pickup.getEffectiveLng() != null) return pickup.getEffectiveLng();
        return 0.0;
    }

    public double getEffectiveDropoffLat() {
        if (dropoffLatitude != null) return dropoffLatitude;
        if (destination != null && destination.getEffectiveLat() != null) return destination.getEffectiveLat();
        return 0.0;
    }

    public double getEffectiveDropoffLng() {
        if (dropoffLongitude != null) return dropoffLongitude;
        if (destination != null && destination.getEffectiveLng() != null) return destination.getEffectiveLng();
        return 0.0;
    }
}