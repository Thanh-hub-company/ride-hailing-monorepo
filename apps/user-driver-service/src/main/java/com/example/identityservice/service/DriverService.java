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

import java.util.List;
import java.util.UUID;

@Service
public class DriverService {

    private final DriverProfileRepository driverProfileRepository;
    private final UserRepository userRepository;

    public DriverService(DriverProfileRepository driverProfileRepository,
                         UserRepository userRepository) {
        this.driverProfileRepository = driverProfileRepository;
        this.userRepository = userRepository;
    }

    /**
     * Lấy thông tin hồ sơ tài xế theo User ID
     */
    public DriverProfileResponse getDriverByUserId(UUID userId) {
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

    /**
     * Cập nhật trạng thái hoạt động tài xế (AVAILABLE, BUSY, OFFLINE)
     */
    @Transactional
    public DriverProfileResponse updateDriverStatus(UUID userId, DriverStatus newStatus) {
        DriverProfile profile = driverProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy hồ sơ tài xế"));

        if (Boolean.FALSE.equals(profile.getIsApproved())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tài xế chưa được duyệt hồ sơ bởi Admin");
        }

        profile.setStatus(newStatus);
        driverProfileRepository.save(profile);

        return getDriverByUserId(userId);
    }

    /**
     * Phe duyệt tài xế (Chức năng dành cho Admin)
     */
    @Transactional
    public DriverProfileResponse approveDriver(UUID driverProfileId) {
        DriverProfile profile = driverProfileRepository.findById(driverProfileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy hồ sơ tài xế"));

        profile.setIsApproved(true);
        driverProfileRepository.save(profile);

        return getDriverByUserId(profile.getUserId());
    }

    /**
     * Lấy danh sách tất cả các tài xế đang sẵn sàng nhận chuyến (AVAILABLE)
     */
    public List<DriverProfileResponse> getAvailableDrivers() {
        return driverProfileRepository.findAll().stream()
                .filter(p -> p.getStatus() == DriverStatus.AVAILABLE && Boolean.TRUE.equals(p.getIsApproved()))
                .map(p -> getDriverByUserId(p.getUserId()))
                .toList();
    }
}