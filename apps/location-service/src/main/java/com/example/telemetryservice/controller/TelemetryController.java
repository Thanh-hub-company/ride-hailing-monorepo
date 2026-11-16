package com.example.telemetryservice.controller;

import com.example.commonsecurity.annotation.CurrentUser;
import com.example.commonsecurity.model.UserPrincipal;
import com.example.telemetryservice.dto.LocationUpdateDto;
import com.example.telemetryservice.service.LocationTrackingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/telemetry")
public class TelemetryController {

    private final LocationTrackingService locationTrackingService;

    public TelemetryController(LocationTrackingService locationTrackingService) {
        this.locationTrackingService = locationTrackingService;
    }

    @PostMapping("/location")
    public ResponseEntity<Void> updateLocation(@CurrentUser UserPrincipal currentUser,
                                               @RequestBody LocationUpdateDto request) {
        UUID driverId = (currentUser != null && currentUser.getUserId() != null)
                ? currentUser.getUserId()
                : request.driverId();

        LocationUpdateDto dto = new LocationUpdateDto(
                driverId,
                request.latitude(),
                request.longitude(),
                request.headingDegrees(),
                request.speedMetersPerSecond(),
                Instant.now()
        );
        locationTrackingService.updateDriverLocation(dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<LocationUpdateDto>> getNearbyDrivers(@RequestParam double latitude,
                                                                    @RequestParam double longitude,
                                                                    @RequestParam(defaultValue = "5.0") double radiusKm) {
        return ResponseEntity.ok(locationTrackingService.findNearbyDrivers(latitude, longitude, radiusKm));
    }

    @DeleteMapping("/location")
    public ResponseEntity<Void> removeLocation(@CurrentUser UserPrincipal currentUser) {
        if (currentUser != null && currentUser.getUserId() != null) {
            locationTrackingService.removeDriverLocation(currentUser.getUserId());
        }
        return ResponseEntity.ok().build();
    }
}
