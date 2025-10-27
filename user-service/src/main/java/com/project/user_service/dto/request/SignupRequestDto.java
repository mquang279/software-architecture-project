package com.project.user_service.dto.request;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SignupRequestDto {
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String password;
}
