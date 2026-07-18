package com.vibeus.gateway.filter;

import com.vibeus.gateway.security.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Validates the JWT on every protected request, then forwards the caller's
 * identity to downstream services via trusted headers. Public endpoints and
 * public catalog reads bypass authentication.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final List<String> PUBLIC_ANY_METHOD_PATHS = List.of(
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/actuator/health"
    );

    private static final List<String> PUBLIC_GET_PATHS = List.of(
            "/api/v1/artists/**",
            "/api/v1/genres/**",
            "/api/v1/albums/**",
            "/api/v1/tracks/**"
    );

    private final JwtUtils jwtUtils;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();
        HttpMethod method = exchange.getRequest().getMethod();

        if (isPublicEndpoint(path, method)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);

        if (!jwtUtils.isTokenValid(token)) {
            return unauthorized(exchange);
        }

        Claims claims = jwtUtils.extractAllClaims(token);

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(request -> request
                        .header("X-User-Id", claims.getSubject())
                        .header("X-User-Email", claims.get("email", String.class))
                        .header("X-User-Name", claims.get("username", String.class))
                        .headers(headers -> headers.remove(HttpHeaders.AUTHORIZATION)))
                .build();

        log.debug("Authenticated request userId={} method={} path={}",
                claims.getSubject(), method, path);

        return chain.filter(mutatedExchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private boolean isPublicEndpoint(String path, HttpMethod method) {
        boolean publicAnyMethod = PUBLIC_ANY_METHOD_PATHS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));

        if (publicAnyMethod) {
            return true;
        }

        if (HttpMethod.GET.equals(method)) {
            return PUBLIC_GET_PATHS.stream()
                    .anyMatch(pattern -> pathMatcher.match(pattern, path));
        }

        return false;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
