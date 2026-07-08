package com.vibeus.auth.dto.request;

import java.util.UUID;

public record CreateUserProfileRequest(
        UUID id,
        String username,
        String email
) {
}
