package com.vibeus.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateUserProfileRequest(

        @NotNull
        UUID id,

        @NotBlank
        String username,

        @NotBlank
        @Email
        String email
) {
}
