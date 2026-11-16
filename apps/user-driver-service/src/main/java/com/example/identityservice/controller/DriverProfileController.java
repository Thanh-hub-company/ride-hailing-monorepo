package com.example.identityservice.controller;

import com.example.commonsecurity.annotation.CurrentUser;
import com.example.commonsecurity.model.UserPrincipal;
import com.example.identityservice.dto.DriverProfileResponse;
import com.example.identityservice.dto.UpdateDriverStatusRequest;
import com.example.identityservice.service.DriverProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/v1/drivers")
public class DriverProfileController {

    private final DriverProfileService driverProfileService;

    public DriverProfileController(DriverProfileService driverProfileService) {
        this.driverProfileService = driverProfileService;
    }

    @GetMapping("/me")
    public ResponseEntity<DriverProfileResponse> getMyProfile(@CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(driverProfileService.getProfileByUserId(currentUser.getUserId()));
    }

    @GetMapping("/available")
    public ResponseEntity<List<DriverProfileResponse>> getAvailableDrivers() {
        return ResponseEntity.ok(driverProfileService.getAvailableDrivers());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<DriverProfileResponse> getDriverProfileById(@PathVariable UUID userId) {
        return ResponseEntity.ok(driverProfileService.getProfileByUserId(userId));
    }

    @PutMapping("/me/status")
    public ResponseEntity<DriverProfileResponse> updateStatus(@CurrentUser UserPrincipal currentUser,
                                                              @RequestBody UpdateDriverStatusRequest request) {
        return ResponseEntity.ok(driverProfileService.updateDriverStatus(currentUser.getUserId(), request.status()));
    }
}