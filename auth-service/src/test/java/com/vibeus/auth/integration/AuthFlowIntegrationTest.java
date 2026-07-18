package com.vibeus.auth.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibeus.auth.client.UserServiceClient;
import com.vibeus.auth.dto.request.CreateUserProfileRequest;
import com.vibeus.auth.dto.request.LoginRequest;
import com.vibeus.auth.dto.request.RegisterRequest;
import com.vibeus.auth.dto.response.UserProfileResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the register -> login flow end-to-end (controller -> service ->
 * repository -> H2) with the downstream user-service call mocked.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserServiceClient userServiceClient;

    @Test
    void register_thenLogin_shouldIssueAccessToken() throws Exception {
        String email = "integration+" + UUID.randomUUID() + "@example.com";
        String username = "user" + UUID.randomUUID().toString().substring(0, 8);

        when(userServiceClient.createUserProfile(any(CreateUserProfileRequest.class)))
                .thenAnswer(inv -> {
                    CreateUserProfileRequest req = inv.getArgument(0);
                    return new UserProfileResponse(req.id(), req.username(), req.email());
                });

        RegisterRequest registerRequest = new RegisterRequest(email, username, "password123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value(email));

        LoginRequest loginRequest = new LoginRequest(email, "password123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    void login_shouldRejectWrongPassword() throws Exception {
        String email = "wrongpass+" + UUID.randomUUID() + "@example.com";
        String username = "user" + UUID.randomUUID().toString().substring(0, 8);

        when(userServiceClient.createUserProfile(any(CreateUserProfileRequest.class)))
                .thenAnswer(inv -> {
                    CreateUserProfileRequest req = inv.getArgument(0);
                    return new UserProfileResponse(req.id(), req.username(), req.email());
                });

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest(email, username, "password123"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest(email, "wrong-password"))))
                .andExpect(status().isUnauthorized());
    }
}
