package com.vibeus.music.dto.request;

import com.vibeus.music.enums.AlbumType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record CreateAlbumRequest(

        @NotBlank
        @Size(max = 200)
        String title,

        @NotNull
        AlbumType type,

        @NotNull
        LocalDate releaseDate,

        String coverImageUrl,

        @NotNull
        UUID artistId
) {
}
