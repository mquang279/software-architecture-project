package com.project.movie_reservation_system.service;

import com.project.movie_reservation_system.dto.PaginationResponse;
import com.project.movie_reservation_system.dto.UserResponseDto;

import java.util.List;

public interface UserService {
    UserResponseDto getCurrentUser();

    PaginationResponse<UserResponseDto> getAllUser(int page, int pageSize);

    UserResponseDto promoteUserToAdmin(String username);
}
