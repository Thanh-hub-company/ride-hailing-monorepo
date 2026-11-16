package com.example.identityservice.service;

import com.example.commonsecurity.config.JwtProperties;
import com.example.commonsecurity.service.JwtTokenProvider;
import com.example.identityservice.domain.entity.User;
import com.example.identityservice.domain.repository.RefreshTokenRepository;
import com.example.identityservice.domain.repository.UserRepository;
import com.example.identityservice.dto.AuthResponse;
import com.example.identityservice.dto.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private AuthService authService;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider jwtTokenProvider;
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        jwtTokenProvider = new JwtTokenProvider(new JwtProperties());
        refreshTokenRepository = Mockito.mock(RefreshTokenRepository.class);
        authService = new AuthService(userRepository, Mockito.mock(com.example.identityservice.domain.repository.DriverProfileRepository.class), passwordEncoder, jwtTokenProvider, refreshTokenRepository);
    }

    @Test
    void testLoginReturnsDualTokens() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setPasswordHash("encodedPassword");
        user.setRole("CUSTOMER");

        when(userRepository.findByIdentifier(any())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);

        LoginRequest request = new LoginRequest("user", null, null, null, "password");
        AuthResponse response = authService.login(request);

        assertNotNull(response.accessToken());
        assertNotNull(response.refreshToken());
    }

    @Test
    void testLoginWithPlainTextPasswordFallback() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setPasswordHash("Password123!");
        user.setRole("DRIVER");

        when(userRepository.findByIdentifier(any())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "Password123!")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("$2a$10$encodedNew");

        LoginRequest request = new LoginRequest(null, "driver1@example.com", null, null, "Password123!");
        AuthResponse response = authService.login(request);

        assertNotNull(response.accessToken());
        assertNotNull(response.refreshToken());
        Mockito.verify(userRepository).save(user);
    }
}
