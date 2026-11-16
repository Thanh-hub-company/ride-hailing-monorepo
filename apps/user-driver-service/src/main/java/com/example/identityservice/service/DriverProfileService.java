package com.example.identityservice.service;

import com.example.commoncontracts.enums.DriverStatus;
import com.example.identityservice.domain.entity.DriverProfile;
import com.example.identityservice.domain.entity.User;
import com.example.identityservice.domain.repository.DriverProfileRepository;
import com.example.identityservice.domain.repository.UserRepository;
import com.example.identityservice.dto.DriverProfileResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.List;

@Service
public class DriverProfileService {

    private final DriverProfileRepository driverProfileRepository;
    private final UserRepository userRepository;

    public DriverProfileService(DriverProfileRepository driverProfileRepository, UserRepository userRepository) {
        this.driverProfileRepository = driverProfileRepository;
        this.userRepository = userRepository;
    }

    public DriverProfileResponse getProfileByUserId(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin người dùng"));

        DriverProfile profile = driverProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy hồ sơ tài xế"));

        return new DriverProfileResponse(
                profile.getId(),
                user.getId(),
                user.getFullName(),
                user.getPhoneNumber(),
                profile.getVehicleLicensePlate(),
                profile.getVehicleModel(),
                profile.getStatus(),
                profile.getRating(),
                profile.getIsApproved()
        );
    }

    @Transactional
    public DriverProfileResponse updateDriverStatus(UUID userId, DriverStatus newStatus) {
        DriverProfile profile = driverProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy hồ sơ tài xế"));

        profile.setStatus(newStatus);
        driverProfileRepository.save(profile);

        return getProfileByUserId(userId);
    }

    public List<DriverProfileResponse> getAvailableDrivers() {
        return driverProfileRepository.findAll().stream()
                .filter(profile -> profile.getStatus() == DriverStatus.AVAILABLE
                        && Boolean.TRUE.equals(profile.getIsApproved()))
                .map(profile -> getProfileByUserId(profile.getUserId()))
                .toList();
    }
}