package com.project.auth_service.dto.response;

import com.project.auth_service.dto.entity.UserDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    String accessToken;
    UserDTO user;
}
