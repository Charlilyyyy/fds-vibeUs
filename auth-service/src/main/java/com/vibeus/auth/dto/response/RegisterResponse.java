package com.vibeus.auth.dto.response;

import java.util.UUID;

public record RegisterResponse(
        UUID userId,
        String email,
        String username,
        String message
) {
}
