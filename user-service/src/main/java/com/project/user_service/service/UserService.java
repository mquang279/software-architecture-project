package com.project.user_service.service;

import com.project.user_service.dto.entity.UserDTO;
import com.project.user_service.dto.request.CreateUserRequest;
import com.project.user_service.dto.request.RegistrationRequest;
import com.project.user_service.dto.request.UpdateUserRequest;
import com.project.user_service.dto.response.PaginationResponse;
import com.project.user_service.entity.User;

public interface UserService {
    PaginationResponse<UserDTO> getAllUser(int page, int pageSize);

    UserDTO createUser(CreateUserRequest request);

    UserDTO createUser(RegistrationRequest request);

    UserDTO getUserById(Long id);

    UserDTO getUserByEmail(String email);

    UserDTO findByRefreshToken(String refreshToken);

    void updateRefreshToken(Long id, String refreshToken);

    UserDTO updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);

    UserDTO convertToDTO(User user);
}
