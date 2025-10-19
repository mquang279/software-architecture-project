package com.project.movie_reservation_system.service;

import com.project.movie_reservation_system.entity.User;

import io.jsonwebtoken.Claims;

public interface JwtService {
    String generateToken(User user);

    Claims extractAllClaims(String token);

    String extractUsername(String token);
}
