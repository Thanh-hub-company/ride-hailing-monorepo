package com.example.tripservice.dto;

import com.example.commoncontracts.enums.TripStatus;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateTripStatusRequest(
        @JsonProperty("targetStatus")
        @JsonAlias({"status", "target_status", "targetStatus", "tripStatus", "trip_status"})
        TripStatus targetStatus
) {
    @JsonCreator
    public UpdateTripStatusRequest(
            @JsonProperty("targetStatus")
            @JsonAlias({"status", "target_status", "targetStatus", "tripStatus", "trip_status"})
            Object targetStatus) {
        this(parseStatus(targetStatus));
    }

    private static TripStatus parseStatus(Object obj) {
        if (obj == null) return null;
        if (obj instanceof TripStatus ts) return ts;
        String str = obj.toString().trim();
        for (TripStatus ts : TripStatus.values()) {
            if (ts.name().equalsIgnoreCase(str) || ts.name().replace("_", "").equalsIgnoreCase(str.replace("_", "").replace(" ", "").replace("-", ""))) {
                return ts;
            }
        }
        return null;
    }
}