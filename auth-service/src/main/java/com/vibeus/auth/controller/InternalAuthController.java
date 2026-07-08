package com.vibeus.auth.controller;

import com.vibeus.auth.dto.request.ValidateTokenRequest;
import com.vibeus.auth.dto.response.TokenValidationResponse;
import com.vibeus.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal-only endpoints for service-to-service calls.
 * Not exposed through the public API gateway.
 */
@RestController
@RequestMapping("/internal/auth")
@RequiredArgsConstructor
public class InternalAuthController {

    private final AuthService authService;

    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(
            @Valid @RequestBody ValidateTokenRequest request) {

        TokenValidationResponse response = authService.validateToken(request);
        return ResponseEntity.ok(response);
    }
}
