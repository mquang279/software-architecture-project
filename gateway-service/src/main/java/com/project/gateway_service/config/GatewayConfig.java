package com.project.gateway_service.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfig {
        @Bean
        public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
                return builder.routes()
                                // Auth Service - Medium rate limit
                                .route("auth-service", r -> r
                                                .path("/auth/**")
                                                // .filters(f -> f
                                                // .requestRateLimiter(c -> c
                                                // .setRateLimiter(redisRateLimiter())
                                                // .setKeyResolver(userKeyResolver()))
                                                // .circuitBreaker(c -> c
                                                // .setName("AuthCircuitBreaker")
                                                // .setFallbackUri("forward:/fallback/auth")))
                                                .uri("lb://auth-service"))

                                // User Service - Standard rate limit
                                .route("user-service", r -> r
                                                .path("/api/v1/users/**")
                                                // .filters(f -> f
                                                // .requestRateLimiter(c -> c
                                                // .setRateLimiter(redisRateLimiter())
                                                // .setKeyResolver(userKeyResolver()))
                                                // .circuitBreaker(c -> c
                                                // .setName("UserCircuitBreaker")
                                                // .setFallbackUri("forward:/fallback/generic")))
                                                .uri("lb://user-service"))

                                // Movie Service - High rate limit (read-heavy)
                                .route("movie-service", r -> r
                                                .path("/api/v1/movies/**")
                                                // .filters(f -> f
                                                // .requestRateLimiter(c -> c
                                                // .setRateLimiter(redisRateLimiter())
                                                // .setKeyResolver(ipKeyResolver()))
                                                // .circuitBreaker(c -> c
                                                // .setName("MovieCircuitBreaker")
                                                // .setFallbackUri("forward:/fallback/generic")))
                                                .uri("lb://movie-service"))

                                // Theater Service - High rate limit (read-heavy)
                                .route("theater-service", r -> r
                                                .path("/api/v1/theaters/**")
                                                // .filters(f -> f
                                                // .requestRateLimiter(c -> c
                                                // .setRateLimiter(redisRateLimiter())
                                                // .setKeyResolver(ipKeyResolver()))
                                                // .circuitBreaker(c -> c
                                                // .setName("TheaterCircuitBreaker")
                                                // .setFallbackUri("forward:/fallback/generic")))
                                                .uri("lb://theater-service"))

                                // Show Service - High rate limit (read-heavy)
                                .route("show-service", r -> r
                                                .path("/api/v1/shows/**")
                                                // .filters(f -> f
                                                // .requestRateLimiter(c -> c
                                                // .setRateLimiter(redisRateLimiter())
                                                // .setKeyResolver(ipKeyResolver()))
                                                // .circuitBreaker(c -> c
                                                // .setName("ShowCircuitBreaker")
                                                // .setFallbackUri("forward:/fallback/generic")))
                                                .uri("lb://show-service"))

                                // Reservation Service - Strict rate limit (write-heavy)
                                .route("reservation-service", r -> r
                                                .path("/api/v1/reservations/**")
                                                // .filters(f -> f
                                                // .requestRateLimiter(c -> c
                                                // .setRateLimiter(strictRedisRateLimiter())
                                                // .setKeyResolver(userKeyResolver()))
                                                // .circuitBreaker(c -> c
                                                // .setName("ReservationCircuitBreaker")
                                                // .setFallbackUri("forward:/fallback/reservation")))
                                                .uri("lb://reservation-service"))

                                // Seat Service - Standard rate limit
                                .route("seat-service", r -> r
                                                .path("/api/v1/seats/**")
                                                // .filters(f -> f
                                                // .requestRateLimiter(c -> c
                                                // .setRateLimiter(redisRateLimiter())
                                                // .setKeyResolver(ipKeyResolver()))
                                                // .circuitBreaker(c -> c
                                                // .setName("SeatCircuitBreaker")
                                                // .setFallbackUri("forward:/fallback/generic")))
                                                .uri("lb://seat-service"))

                                // Notification Service - Standard rate limit
                                .route("notification-service", r -> r
                                                .path("/api/v1/notifications/**")
                                                // .filters(f -> f
                                                // .requestRateLimiter(c -> c
                                                // .setRateLimiter(redisRateLimiter())
                                                // .setKeyResolver(userKeyResolver()))
                                                // .circuitBreaker(c -> c
                                                // .setName("NotificationCircuitBreaker")
                                                // .setFallbackUri("forward:/fallback/generic")))
                                                .uri("lb://notification-service"))

                                // Payment Service - Very strict rate limit (nếu có)
                                .route("payment-service", r -> r
                                                .path("/api/v1/payments/**")
                                                // .filters(f -> f
                                                // .requestRateLimiter(c -> c
                                                // .setRateLimiter(paymentRateLimiter())
                                                // .setKeyResolver(userKeyResolver()))
                                                // .circuitBreaker(c -> c
                                                // .setName("PaymentCircuitBreaker")
                                                // .setFallbackUri("forward:/fallback/payment")))
                                                .uri("lb://payment-service"))
                                .build();
        }

        @Bean
        @Primary
        public RedisRateLimiter redisRateLimiter() {
                return new RedisRateLimiter(200, 500); // 200 requests per second, burst 500
        }

        @Bean
        public RedisRateLimiter strictRedisRateLimiter() {
                return new RedisRateLimiter(10, 20); // 10 requests per second, burst 20
        }

        @Bean
        public RedisRateLimiter paymentRateLimiter() {
                return new RedisRateLimiter(1, 2); // 1 request per second, burst of 2
        }

        @Bean
        @Primary
        public KeyResolver userKeyResolver() {
                return exchange -> {
                        // Try to get user ID from JWT or session
                        String userId = exchange.getRequest().getHeaders()
                                        .getFirst("X-User-Id");

                        if (userId != null) {
                                return Mono.just("user:" + userId);
                        }

                        // Fall back to IP address
                        String ip = exchange.getRequest().getRemoteAddress() != null
                                        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                                        : "unknown";
                        return Mono.just("ip:" + ip);
                };
        }

        @Bean
        public KeyResolver ipKeyResolver() {
                return exchange -> {
                        String ip = exchange.getRequest().getRemoteAddress() != null
                                        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                                        : "unknown";
                        return Mono.just(ip);
                };
        }
}
