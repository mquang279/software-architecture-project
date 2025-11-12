package com.project.auth_service.client;
import org.springframework.stereotype.Component;

import com.project.auth_service.dto.entity.UserDTO;
import com.project.auth_service.dto.request.RegistrationRequest;

@Component
public class UserServiceFallback implements UserService {

    @Override
    public UserDTO getUserByEmail(String email) {
        throw new RuntimeException("User service is unavailable. Please try again later.");
    }

    @Override
    public UserDTO getUserByRefreshToken(String token) {
        throw new RuntimeException("User service is unavailable. Please try again later.");
    }

    @Override
    public UserDTO createUser(RegistrationRequest request) {
        throw new RuntimeException("User service is unavailable. Please try again later.");
    }

    @Override
    public void updateRefreshToken(Long id, String token) {
        System.out.println("Skipped refresh token update - User service u   navailable");
    }
}
