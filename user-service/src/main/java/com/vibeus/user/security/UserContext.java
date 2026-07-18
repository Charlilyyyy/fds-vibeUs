package com.vibeus.user.security;

import com.vibeus.user.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves the authenticated caller from headers injected by the API gateway
 * after it validates the JWT. Downstream services trust these headers because
 * only the gateway can reach them on the internal network.
 */
@Component
@RequiredArgsConstructor
public class UserContext {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_NAME = "X-User-Name";
    private static final String HEADER_USER_EMAIL = "X-User-Email";

    private final HttpServletRequest request;

    public UUID getCurrentUserId() {
        String userId = request.getHeader(HEADER_USER_ID);

        if (userId == null || userId.isBlank()) {
            throw new UnauthorizedException("Missing " + HEADER_USER_ID + " header");
        }

        return UUID.fromString(userId);
    }

    public String getCurrentUsername() {
        return request.getHeader(HEADER_USER_NAME);
    }

    public String getCurrentUserEmail() {
        return request.getHeader(HEADER_USER_EMAIL);
    }
}
