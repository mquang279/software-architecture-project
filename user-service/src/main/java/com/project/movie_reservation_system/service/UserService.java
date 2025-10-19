package com.project.movie_reservation_system.service;

import com.project.movie_reservation_system.dto.entity.UserDTO;
import com.project.movie_reservation_system.dto.response.PaginationResponse;
import com.project.movie_reservation_system.dto.response.UserResponseDto;
import com.project.movie_reservation_system.entity.User;

public interface UserService {
    UserResponseDto getCurrentUser();

    PaginationResponse<UserResponseDto> getAllUser(int page, int pageSize);

    UserDTO createUser(User user);

    UserDTO getUserById(Long id);

    UserDTO updateUser(Long id, User user);

    void deleteUser(Long id);

    UserDTO convertToDTO(User user);
}
