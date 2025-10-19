package com.project.movie_reservation_system.dto;

import com.project.movie_reservation_system.entity.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private long id;
    private String firstName;
    private String lastname;
    private String username;
    private String email;

    public UserDTO(User user) {
        this.setId(user.getId());
        this.setFirstName(user.getFirstName());
        this.setLastname(user.getLastName());
        this.setUsername(user.getUsername());
        this.setEmail(user.getEmail());
    }
}
