package com.example.identityservice.service;

import com.example.identityservice.domain.entity.DriverProfile;
import com.example.identityservice.domain.entity.User;
import com.example.identityservice.domain.repository.DriverProfileRepository;
import com.example.identityservice.domain.repository.UserRepository;
import com.example.identityservice.dto.AuthResponse;
import com.example.identityservice.dto.LoginRequest;
import com.example.identityservice.dto.RegisterRequest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.example.commonsecurity.service.JwtTokenProvider;
import com.example.identityservice.domain.entity.RefreshToken;
import com.example.identityservice.domain.repository.RefreshTokenRepository;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthService(UserRepository userRepository,
                       DriverProfileRepository driverProfileRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String phone = request.phoneNumber() != null && !request.phoneNumber().isBlank()
                ? request.phoneNumber().trim()
                : (request.phone() != null && !request.phone().isBlank() ? request.phone().trim() : null);

        String email = request.email() != null && !request.email().isBlank()
                ? request.email().trim().toLowerCase()
                : null;

        if (phone == null && email == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cần cung cấp ít nhất Số điện thoại hoặc Email để đăng ký");
        }

        if (phone != null && userRepository.existsByPhoneNumber(phone)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số điện thoại đã tồn tại trên hệ thống");
        }

        if (email != null && userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email đã tồn tại trên hệ thống");
        }

        User user = new User();
        user.setEmail(email);
        user.setPhoneNumber(phone);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setRole(request.role() != null ? request.role().toUpperCase() : "CUSTOMER");
        User savedUser = userRepository.save(user);

        if ("DRIVER".equalsIgnoreCase(request.role())) {
            DriverProfile profile = new DriverProfile();
            profile.setUserId(savedUser.getId());
            profile.setVehicleLicensePlate(request.vehicleLicensePlate());
            profile.setVehicleModel(request.vehicleModel());
            driverProfileRepository.save(profile);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(savedUser.getId().toString(), getClaims(savedUser));
        String refreshToken = jwtTokenProvider.generateRefreshToken(savedUser.getId().toString());

        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setToken(refreshToken);
        refreshTokenEntity.setUserId(savedUser.getId());
        refreshTokenEntity.setExpiryDate(Instant.now().plusSeconds(604800)); // 7 days
        refreshTokenRepository.save(refreshTokenEntity);

        return new AuthResponse(accessToken, refreshToken, savedUser.getId(), savedUser.getFullName(), savedUser.getRole());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String loginKey = null;
        if (request.identifier() != null && !request.identifier().isBlank()) {
            loginKey = request.identifier().trim();
        } else if (request.email() != null && !request.email().isBlank()) {
            loginKey = request.email().trim();
        } else if (request.phoneNumber() != null && !request.phoneNumber().isBlank()) {
            loginKey = request.phoneNumber().trim();
        } else if (request.phone() != null && !request.phone().isBlank()) {
            loginKey = request.phone().trim();
        }

        if (loginKey == null || request.password() == null || request.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sai thông tin đăng nhập hoặc mật khẩu");
        }

        String searchKey = loginKey;
        User user = userRepository.findByIdentifier(searchKey)
                .or(() -> userRepository.findByPhoneNumber(searchKey))
                .or(() -> userRepository.findByEmailIgnoreCase(searchKey))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sai thông tin đăng nhập hoặc mật khẩu"));

        String inputPassword = request.password();
        boolean passwordMatches = passwordEncoder.matches(inputPassword, user.getPasswordHash())
                || (user.getPasswordHash() != null && user.getPasswordHash().equals(inputPassword));

        if (!passwordMatches) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sai thông tin đăng nhập hoặc mật khẩu");
        }

        // Tự động nâng cấp hash mật khẩu nếu trong DB đang là plain text
        if (user.getPasswordHash() != null && user.getPasswordHash().equals(inputPassword)) {
            user.setPasswordHash(passwordEncoder.encode(inputPassword));
            userRepository.save(user);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId().toString(), getClaims(user));
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId().toString());

        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setToken(refreshToken);
        refreshTokenEntity.setUserId(user.getId());
        refreshTokenEntity.setExpiryDate(Instant.now().plusSeconds(604800)); // 7 days
        refreshTokenRepository.save(refreshTokenEntity);

        return new AuthResponse(accessToken, refreshToken, user.getId(), user.getFullName(), user.getRole());
    }

    @Transactional
    public AuthResponse refresh(String refreshToken) {
        RefreshToken tokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token không hợp lệ"));

        if (tokenEntity.isRevoked() || tokenEntity.getExpiryDate().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token đã hết hạn hoặc bị thu hồi");
        }

        User user = userRepository.findById(tokenEntity.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User không tồn tại"));

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId().toString(), getClaims(user));
        return new AuthResponse(newAccessToken, refreshToken, user.getId(), user.getFullName(), user.getRole());
    }

    private Map<String, Object> getClaims(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole());
        if (user.getPhoneNumber() != null) claims.put("phoneNumber", user.getPhoneNumber());
        if (user.getEmail() != null) claims.put("email", user.getEmail());
        return claims;
    }
}