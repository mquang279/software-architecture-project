package com.project.user_service.dto.request;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    private String username;
    private String email;
}
