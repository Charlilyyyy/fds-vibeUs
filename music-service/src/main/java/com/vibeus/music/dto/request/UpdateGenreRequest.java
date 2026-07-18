package com.vibeus.music.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateGenreRequest(

        @Size(max = 100)
        String name,

        @Size(max = 1000)
        String description
) {
}
