package com.vibeus.music.dto.response;

public record ApiResponse<T>(
        int status,
        String message,
        T data
) {
}
