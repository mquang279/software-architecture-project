package com.project.movie_reservation_system.dto.request;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String firstName;
    private String lastName;
    private String username;
    private String email;
}
