package com.vibeus.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Value("${services.auth-service.url:http://localhost:8082}")
    private String authServiceUrl;

    @Value("${services.user-service.url:http://localhost:8083}")
    private String userServiceUrl;

    @Value("${services.music-service.url:http://localhost:8084}")
    private String musicServiceUrl;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r
                        .path("/api/v1/auth/**")
                        .uri(authServiceUrl))
                .route("user-service", r -> r
                        .path("/api/v1/users/**")
                        .uri(userServiceUrl))
                .route("music-service", r -> r
                        .path("/api/v1/artists/**",
                                "/api/v1/genres/**",
                                "/api/v1/albums/**",
                                "/api/v1/tracks/**",
                                "/api/v1/uploads/**")
                        .uri(musicServiceUrl))
                .build();
    }
}
