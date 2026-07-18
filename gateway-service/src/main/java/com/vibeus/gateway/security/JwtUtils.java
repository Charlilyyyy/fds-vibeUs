package com.vibeus.gateway.security;

import com.vibeus.gateway.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Verifies access tokens issued by the auth-service and extracts identity
 * claims so the gateway can forward them as trusted headers.
 */
@Component
@RequiredArgsConstructor
public class JwtUtils {

    private static final String CLAIM_TYPE = "type";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_USERNAME = "username";
    private static final String ACCESS_TYPE = "access";

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return ACCESS_TYPE.equals(claims.get(CLAIM_TYPE, String.class));
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).get(CLAIM_EMAIL, String.class);
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).get(CLAIM_USERNAME, String.class);
    }
}
