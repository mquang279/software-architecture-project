package com.project.auth_service.service;

import com.project.auth_service.dto.entity.UserDTO;

public interface AuthService {
    String generateAccessToken(String email, UserDTO user);

    String generateRefreshToken(String email, UserDTO user);

    String getCurrentUserEmail();
}
