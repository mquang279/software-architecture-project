package com.project.user_service.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthRequestDto {
    String username;
    String password;
}
