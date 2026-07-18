package com.vibeus.music.dto.response;

import java.util.List;
import java.util.UUID;

public record TrackResponse(
        UUID id,
        String title,
        Integer durationInSeconds,
        String audioUrl,
        String coverImageUrl,
        Long playCount,
        UUID albumId,
        String albumTitle,
        List<ArtistSummaryResponse> artists,
        List<GenreSummaryResponse> genres
) {
}
