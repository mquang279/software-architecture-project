package com.project.movie_reservation_system.service;

import com.project.movie_reservation_system.dto.AuthResponseDto;
import com.project.movie_reservation_system.dto.SignupRequestDto;

public interface AuthService {
    AuthResponseDto signup(SignupRequestDto signupRequestDto);

    AuthResponseDto authenticateUser(String username);
}
