package com.vibeus.auth.service.impl;

import com.vibeus.auth.config.JwtProperties;
import com.vibeus.auth.entity.User;
import com.vibeus.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private static final String CLAIM_TYPE = "type";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLES = "roles";
    private static final String ACCESS_TYPE = "access";
    private static final String ROLE_USER = "ROLE_USER";

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.accessTokenExpiration());

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim(CLAIM_EMAIL, user.getEmail())
                .claim(CLAIM_USERNAME, user.getUsername())
                .claim(CLAIM_ROLES, List.of(ROLE_USER))
                .claim(CLAIM_TYPE, ACCESS_TYPE)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public String extractSubject(String token) {
        return extractClaims(token).getSubject();
    }

    @Override
    public String extractEmail(String token) {
        return extractClaims(token).get(CLAIM_EMAIL, String.class);
    }

    @Override
    public String extractUsername(String token) {
        return extractClaims(token).get(CLAIM_USERNAME, String.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Object roles = extractClaims(token).get(CLAIM_ROLES);
        if (roles instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    @Override
    public Instant extractExpiration(String token) {
        return extractClaims(token).getExpiration().toInstant();
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            return isAccessToken(token) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    @Override
    public boolean isAccessToken(String token) {
        return ACCESS_TYPE.equals(extractClaims(token).get(CLAIM_TYPE, String.class));
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }
}
