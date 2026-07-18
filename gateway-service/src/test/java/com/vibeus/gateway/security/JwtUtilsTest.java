package com.vibeus.gateway.security;

import com.vibeus.gateway.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilsTest {

    private static final String SECRET = "vibeus-local-dev-jwt-secret-change-me-32chars";

    private JwtUtils jwtUtils;
    private SecretKey key;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(SECRET);
        jwtUtils = new JwtUtils(properties);
        key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void isTokenValid_shouldAcceptAccessToken() {
        String token = Jwts.builder()
                .subject("user-1")
                .claim("type", "access")
                .claim("email", "user@example.com")
                .claim("username", "jazzfan")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();

        assertThat(jwtUtils.isTokenValid(token)).isTrue();
        assertThat(jwtUtils.extractUserId(token)).isEqualTo("user-1");
        assertThat(jwtUtils.extractEmail(token)).isEqualTo("user@example.com");
        assertThat(jwtUtils.extractUsername(token)).isEqualTo("jazzfan");
    }

    @Test
    void isTokenValid_shouldRejectNonAccessToken() {
        String token = Jwts.builder()
                .subject("user-1")
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();

        assertThat(jwtUtils.isTokenValid(token)).isFalse();
    }

    @Test
    void isTokenValid_shouldRejectExpiredToken() {
        String token = Jwts.builder()
                .subject("user-1")
                .claim("type", "access")
                .issuedAt(new Date(System.currentTimeMillis() - 120_000))
                .expiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(key)
                .compact();

        assertThat(jwtUtils.isTokenValid(token)).isFalse();
    }

    @Test
    void isTokenValid_shouldRejectMalformedToken() {
        assertThat(jwtUtils.isTokenValid("not-a-token")).isFalse();
    }
}
