package com.vibeus.music.dto.response;

import com.vibeus.music.enums.AlbumType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AlbumResponse(
        UUID id,
        String title,
        AlbumType type,
        LocalDate releaseDate,
        String coverImageUrl,
        boolean active,
        UUID artistId,
        String artistName,
        Instant createdAt,
        Instant updatedAt
) {
}
