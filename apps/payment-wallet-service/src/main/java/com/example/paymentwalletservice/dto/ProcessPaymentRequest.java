package com.example.paymentwalletservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.UUID;

public record ProcessPaymentRequest(
        @JsonProperty("tripId")
        @JsonAlias({"tripId", "trip_id", "tripID", "id"})
        UUID tripId,

        @JsonProperty("amount")
        @JsonAlias({"amount", "fareAmount", "fare_amount", "totalFare", "total_fare", "price", "fare"})
        BigDecimal amount,

        @JsonProperty("paymentMethod")
        @JsonAlias({"paymentMethod", "payment_method", "method", "paymentType", "payment_type"})
        String paymentMethod,

        @JsonProperty("customerId")
        @JsonAlias({"customerId", "customer_id", "userId", "user_id"})
        UUID customerId
) {
    @JsonCreator
    public ProcessPaymentRequest(
            @JsonProperty("tripId")
            @JsonAlias({"tripId", "trip_id", "tripID", "id"})
            Object tripId,

            @JsonProperty("amount")
            @JsonAlias({"amount", "fareAmount", "fare_amount", "totalFare", "total_fare", "price", "fare"})
            Object amount,

            @JsonProperty("paymentMethod")
            @JsonAlias({"paymentMethod", "payment_method", "method", "paymentType", "payment_type"})
            Object paymentMethod,

            @JsonProperty("customerId")
            @JsonAlias({"customerId", "customer_id", "userId", "user_id"})
            Object customerId
    ) {
        this(parseUuid(tripId), parseBigDecimal(amount), parseString(paymentMethod), parseUuid(customerId));
    }

    public ProcessPaymentRequest(UUID tripId, BigDecimal amount, String paymentMethod) {
        this(tripId, amount, paymentMethod, null);
    }

    private static UUID parseUuid(Object obj) {
        if (obj == null) return null;
        if (obj instanceof UUID u) return u;
        try {
            return UUID.fromString(obj.toString().trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static BigDecimal parseBigDecimal(Object obj) {
        if (obj == null) return null;
        if (obj instanceof BigDecimal bd) return bd;
        if (obj instanceof Number num) return BigDecimal.valueOf(num.doubleValue());
        try {
            String str = obj.toString().trim().replace(",", "");
            return new BigDecimal(str);
        } catch (Exception e) {
            return null;
        }
    }

    private static String parseString(Object obj) {
        return obj != null ? obj.toString().trim() : null;
    }
}