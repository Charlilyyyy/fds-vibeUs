package com.vibeus.music.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateArtistRequest(

        @Size(max = 255)
        String name,

        @Size(max = 3000)
        String bio,

        String imageUrl,

        Boolean verified
) {
}
