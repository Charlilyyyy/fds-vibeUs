package com.vibeus.music.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateArtistRequest(

        @NotBlank
        @Size(max = 255)
        String name,

        @Size(max = 3000)
        String bio,

        String imageUrl
) {
}
