package com.vibeus.user.dto.response;

public record ApiResponse<T>(
        int status,
        String message,
        T data
) {
}
