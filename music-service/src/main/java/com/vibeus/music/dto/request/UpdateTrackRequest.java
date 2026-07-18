package com.vibeus.music.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Set;
import java.util.UUID;

public record UpdateTrackRequest(

        @NotBlank
        String title,

        @NotNull
        @Positive
        Integer durationInSeconds,

        @NotBlank
        String audioUrl,

        String coverImageUrl,

        UUID albumId,

        @NotEmpty
        Set<UUID> artistIds,

        @NotEmpty
        Set<UUID> genreIds
) {
}
