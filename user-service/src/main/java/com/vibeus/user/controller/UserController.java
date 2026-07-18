package com.vibeus.user.controller;

import com.vibeus.user.dto.request.UpdateUserProfileRequest;
import com.vibeus.user.dto.response.ApiResponse;
import com.vibeus.user.dto.response.UserResponse;
import com.vibeus.user.security.UserContext;
import com.vibeus.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserContext userContext;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {

        UserResponse response = userService.getUserById(userContext.getCurrentUserId());

        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(),
                "Profile retrieved",
                response
        ));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUser(
            @Valid @RequestBody UpdateUserProfileRequest request) {

        UserResponse response = userService.updateUserProfile(
                userContext.getCurrentUserId(), request);

        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(),
                "Profile updated",
                response
        ));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable UUID userId) {

        UserResponse response = userService.getUserById(userId);

        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(),
                "Profile retrieved",
                response
        ));
    }
}
