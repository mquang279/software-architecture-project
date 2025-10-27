package com.project.gateway_service.config;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
        @Bean
        public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
                return builder.routes()
                                .route("auth-service", r -> r
                                                .path("/auth/**")
                                                .uri("lb://auth-service"))
                                .route("user-service", r -> r
                                                .path("/users/**")
                                                .uri("lb://user-service"))
                                .route("movie-service", r -> r
                                                .path("/movies/**")
                                                .uri("lb://movie-service"))
                                .route("theater-service", r -> r
                                                .path("/api/v1/theaters/**")
                                                .uri("lb://theater-service"))
                                .route("show-service", r -> r
                                                .path("/api/v1/shows/**")
                                                .uri("lb://show-service"))
                                .route("reservation-service", r -> r
                                                .path("/api/v1/reservations/**")
                                                .uri("lb://reservation-service"))
                                .route("seat-service", r -> r
                                                .path("/api/v1/seats/**")
                                                .uri("lb://seat-service"))
                                .route("notification-service", r -> r
                                        .path("/api/v1/notifications/**")
                                        .uri("lb://notification-service"))
                                .build();
        }
}
