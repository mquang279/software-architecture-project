package com.project.movie_reservation_system.dto;

import com.project.movie_reservation_system.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private long id;
    private String firstName;
    private String lastname;
    private String username;
    private String email;
    private String passwordHash;
    private Role role;
}