package com.project.movie_reservation_system.dto.response;

import com.project.movie_reservation_system.dto.entity.UserDTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponseDto {
    String token;
    UserDTO userDTO;
}
