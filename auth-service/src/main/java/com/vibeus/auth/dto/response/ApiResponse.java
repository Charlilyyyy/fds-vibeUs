package com.vibeus.auth.dto.response;

public record ApiResponse<T>(
        int status,
        String message,
        T data
) {
}
