package com.project.auth_service.dto.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserClaims {
    private Long id;
    private String username;
    private String email;
}
