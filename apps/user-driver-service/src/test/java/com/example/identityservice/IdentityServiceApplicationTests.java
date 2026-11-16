package com.example.identityservice;

import com.example.identityservice.dto.AuthResponse;
import com.example.identityservice.dto.LoginRequest;
import com.example.identityservice.dto.RegisterRequest;
import com.example.identityservice.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:identity-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class IdentityServiceApplicationTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRegisterAndLoginWithEmailAndPhone() {
        // 1. Register with both email and phone
        RegisterRequest registerReq = new RegisterRequest(
                "testuser@example.com",
                "0911223344",
                null,
                "Password123!",
                "Test User",
                "CUSTOMER",
                null,
                null
        );
        AuthResponse regResponse = authService.register(registerReq);
        assertNotNull(regResponse.accessToken());
        assertEquals("Test User", regResponse.fullName());

        // 2. Login using email
        LoginRequest loginByEmail = new LoginRequest(
                null,
                "testuser@example.com",
                null,
                null,
                "Password123!"
        );
        AuthResponse loginEmailResp = authService.login(loginByEmail);
        assertNotNull(loginEmailResp.accessToken());
        assertEquals(regResponse.userId(), loginEmailResp.userId());

        // 3. Login using phone
        LoginRequest loginByPhone = new LoginRequest(
                null,
                null,
                "0911223344",
                null,
                "Password123!"
        );
        AuthResponse loginPhoneResp = authService.login(loginByPhone);
        assertNotNull(loginPhoneResp.accessToken());
        assertEquals(regResponse.userId(), loginPhoneResp.userId());

        // 4. Login using identifier
        LoginRequest loginByIdentifier = new LoginRequest(
                "TESTUSER@example.com",
                null,
                null,
                null,
                "Password123!"
        );
        AuthResponse loginIdResp = authService.login(loginByIdentifier);
        assertNotNull(loginIdResp.accessToken());
        assertEquals(regResponse.userId(), loginIdResp.userId());
    }

    @Test
    void testHttpRegisterAndLoginEndpointsPermitAll() throws Exception {
        RegisterRequest registerReq = new RegisterRequest(
                "http_customer@example.com",
                "0933445566",
                null,
                "Password123!",
                "Http Customer",
                "CUSTOMER",
                null,
                null
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.fullName").value("Http Customer"));

        LoginRequest loginReq = new LoginRequest(
                null,
                "http_customer@example.com",
                null,
                null,
                "Password123!"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void testGetDriverProfileWithCurrentUser() throws Exception {
        RegisterRequest registerDriverReq = new RegisterRequest(
                "http_driver@example.com",
                "0988776655",
                null,
                "Password123!",
                "Http Driver",
                "DRIVER",
                "51A-999.99",
                "Toyota Vios"
        );

        AuthResponse driverAuth = authService.register(registerDriverReq);
        assertNotNull(driverAuth.accessToken());

        mockMvc.perform(get("/api/v1/drivers/me")
                        .header("Authorization", "Bearer " + driverAuth.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Http Driver"))
                .andExpect(jsonPath("$.vehicleLicensePlate").value("51A-999.99"))
                .andExpect(jsonPath("$.vehicleModel").value("Toyota Vios"));
    }

    @Test
    void testDriverProfileRequiresAccessToken() throws Exception {
        mockMvc.perform(get("/api/v1/drivers/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDriverProfileRejectsRefreshToken() throws Exception {
        RegisterRequest registerDriverReq = new RegisterRequest(
                "refresh_driver@example.com",
                "0988776656",
                null,
                "Password123!",
                "Refresh Driver",
                "DRIVER",
                "51A-888.88",
                "Toyota Vios"
        );

        AuthResponse driverAuth = authService.register(registerDriverReq);

        mockMvc.perform(get("/api/v1/drivers/me")
                        .header("Authorization", "Bearer " + driverAuth.refreshToken()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDriverProfileRejectsMalformedToken() throws Exception {
        mockMvc.perform(get("/api/v1/drivers/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }
}
