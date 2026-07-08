package com.vibeus.auth.service;

import com.vibeus.auth.dto.request.LoginRequest;
import com.vibeus.auth.dto.request.RegisterRequest;
import com.vibeus.auth.dto.request.ValidateTokenRequest;
import com.vibeus.auth.dto.response.AuthenticationResponse;
import com.vibeus.auth.dto.response.RegisterResponse;
import com.vibeus.auth.dto.response.TokenValidationResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    AuthenticationResponse login(LoginRequest request);

    TokenValidationResponse validateToken(ValidateTokenRequest request);
}
