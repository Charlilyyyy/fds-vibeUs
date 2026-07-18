package com.vibeus.music.dto.response;

import java.util.UUID;

public record GenreSummaryResponse(
        UUID id,
        String name
) {
}
