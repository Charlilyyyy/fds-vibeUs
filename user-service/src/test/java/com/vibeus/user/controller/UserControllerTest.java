package com.vibeus.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibeus.user.dto.request.UpdateUserProfileRequest;
import com.vibeus.user.dto.response.UserResponse;
import com.vibeus.user.security.UserContext;
import com.vibeus.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserContext userContext;

    @Test
    void getCurrentUser_shouldReturnProfile() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userContext.getCurrentUserId()).thenReturn(userId);
        when(userService.getUserById(userId)).thenReturn(sampleResponse(userId));

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(userId.toString()))
                .andExpect(jsonPath("$.data.username").value("jazzfan"));
    }

    @Test
    void updateCurrentUser_shouldReturnUpdatedProfile() throws Exception {
        UUID userId = UUID.randomUUID();
        UpdateUserProfileRequest request =
                new UpdateUserProfileRequest("Miles", "Davis", "Trumpet", null);

        when(userContext.getCurrentUserId()).thenReturn(userId);
        when(userService.updateUserProfile(eq(userId), any(UpdateUserProfileRequest.class)))
                .thenReturn(sampleResponse(userId));

        mockMvc.perform(put("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(userId.toString()));
    }

    @Test
    void getUserById_shouldReturnProfile() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userService.getUserById(userId)).thenReturn(sampleResponse(userId));

        mockMvc.perform(get("/api/v1/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("jazzfan"));
    }

    private UserResponse sampleResponse(UUID userId) {
        return new UserResponse(
                userId,
                "jazzfan",
                "user@example.com",
                "Miles",
                "Davis",
                "Trumpet",
                null,
                true,
                Instant.parse("2026-07-08T12:00:00Z")
        );
    }
}
