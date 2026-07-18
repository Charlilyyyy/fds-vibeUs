package com.vibeus.user.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(

        @Size(max = 100)
        String firstName,

        @Size(max = 100)
        String lastName,

        @Size(max = 500)
        String bio,

        @Size(max = 500)
        String profileImageUrl
) {
}
