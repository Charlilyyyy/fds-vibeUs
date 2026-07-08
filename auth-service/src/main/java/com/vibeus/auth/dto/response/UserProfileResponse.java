package com.vibeus.auth.dto.response;

import java.util.UUID;

/**
 * Minimal profile response returned by user-service internal create API.
 */
public record UserProfileResponse(
        UUID id,
        String username,
        String email
) {
}
