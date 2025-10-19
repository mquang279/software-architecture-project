package com.project.movie_reservation_system.service;

import com.project.movie_reservation_system.dto.entity.UserDTO;
import com.project.movie_reservation_system.dto.request.CreateUserRequest;
import com.project.movie_reservation_system.dto.request.UpdateUserRequest;
import com.project.movie_reservation_system.dto.response.PaginationResponse;
import com.project.movie_reservation_system.entity.User;

public interface UserService {
    PaginationResponse<UserDTO> getAllUser(int page, int pageSize);

    UserDTO createUser(CreateUserRequest request);

    UserDTO getUserById(Long id);

    UserDTO updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);

    UserDTO convertToDTO(User user);
}
