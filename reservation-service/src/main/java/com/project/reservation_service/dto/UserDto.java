package com.project.reservation_service.dto;

import com.project.reservation_service.enums.Role;
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