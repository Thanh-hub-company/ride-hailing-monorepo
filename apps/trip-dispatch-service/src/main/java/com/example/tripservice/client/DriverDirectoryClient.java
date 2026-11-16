package com.example.tripservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class DriverDirectoryClient {

    private final RestClient restClient;

    public DriverDirectoryClient(
            @Value("${identity-service.base-url:http://localhost:8085}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public List<UUID> getAvailableDriverIds() {
        AvailableDriver[] drivers = restClient.get()
                .uri("/api/v1/drivers/available")
                .retrieve()
                .body(AvailableDriver[].class);
        return drivers == null
                ? List.of()
                : Arrays.stream(drivers).map(AvailableDriver::userId).toList();
    }

    private record AvailableDriver(UUID userId) {
    }
}
