package com.project.movie_reservation_system.client;

import com.project.movie_reservation_system.dto.UserDto;
import org.springframework.stereotype.Component;

@Component
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public UserDto getUserById(Long userId) {
        UserDto fallbackUser = new UserDto();
        fallbackUser.setId(userId);
        fallbackUser.setEmail("unknown@system.com");
        fallbackUser.setFirstName("Unknown User (Service Unavailable)");

        return fallbackUser;
    }
}
