package com.vibeus.music.dto.response;

import java.util.UUID;

public record ArtistSummaryResponse(
        UUID id,
        String name
) {
}
