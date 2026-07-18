package com.vibeus.music.dto.request;

import com.vibeus.music.enums.AlbumType;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateAlbumRequest(

        @Size(max = 200)
        String title,

        AlbumType type,

        LocalDate releaseDate,

        String coverImageUrl,

        UUID artistId
) {
}
