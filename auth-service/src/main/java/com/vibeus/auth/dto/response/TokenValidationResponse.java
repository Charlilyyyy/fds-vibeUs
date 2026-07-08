package com.vibeus.auth.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TokenValidationResponse(
        boolean valid,
        UUID userId,
        String email,
        String username,
        List<String> roles,
        Instant expiresAt
) {

    public static TokenValidationResponse invalid() {
        return new TokenValidationResponse(false, null, null, null, List.of(), null);
    }

    public static TokenValidationResponse valid(
            UUID userId,
            String email,
            String username,
            List<String> roles,
            Instant expiresAt
    ) {
        return new TokenValidationResponse(true, userId, email, username, roles, expiresAt);
    }
}
