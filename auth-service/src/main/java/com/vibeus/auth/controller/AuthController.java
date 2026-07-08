package com.vibeus.auth.controller;

import com.vibeus.auth.dto.request.LoginRequest;
import com.vibeus.auth.dto.request.RegisterRequest;
import com.vibeus.auth.dto.response.ApiResponse;
import com.vibeus.auth.dto.response.AuthenticationResponse;
import com.vibeus.auth.dto.response.RegisterResponse;
import com.vibeus.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        RegisterResponse response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        HttpStatus.CREATED.value(),
                        response.message(),
                        response
                ));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthenticationResponse response = authService.login(request);

        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(),
                "Login successful",
                response
        ));
    }
}
