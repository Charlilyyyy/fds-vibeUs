package com.vibeus.auth.service;

import com.vibeus.auth.entity.User;

import java.time.Instant;
import java.util.List;

public interface JwtService {

    String generateAccessToken(User user);

    String extractSubject(String token);

    String extractEmail(String token);

    String extractUsername(String token);

    List<String> extractRoles(String token);

    Instant extractExpiration(String token);

    boolean isTokenValid(String token);

    boolean isAccessToken(String token);
}
