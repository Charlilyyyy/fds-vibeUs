package com.vibeus.user.controller;

import com.vibeus.user.dto.request.CreateUserProfileRequest;
import com.vibeus.user.dto.response.UserResponse;
import com.vibeus.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal-only endpoints for service-to-service calls (e.g. auth-service
 * creating a profile after registration). Not exposed through the API gateway.
 */
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUserProfile(
            @Valid @RequestBody CreateUserProfileRequest request) {

        UserResponse response = userService.createUserProfile(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
