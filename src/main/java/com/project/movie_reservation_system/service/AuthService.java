package com.project.movie_reservation_system.service;

import com.project.movie_reservation_system.dto.SignupRequestDto;

public interface AuthService {
    String signup(SignupRequestDto signupRequestDto);

    String authenticateUser(String username);
}
