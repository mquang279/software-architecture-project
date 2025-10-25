package com.project.user_service.dto.response;

import com.project.user_service.dto.entity.UserDTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponseDto {
    String token;
    UserDTO userDTO;
}
