package com.vibeus.music.dto.response;

import java.time.Instant;
import java.util.UUID;

public record GenreResponse(
        UUID id,
        String name,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
