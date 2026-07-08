package com.vibeus.auth.dto.response;

import java.time.Instant;
import java.util.UUID;

public record AuthenticationResponse(
        String accessToken,
        String tokenType,
        Instant expiresAt,
        UUID userId
) {
}
