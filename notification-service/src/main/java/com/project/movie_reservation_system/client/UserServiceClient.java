package com.project.movie_reservation_system.client;

import com.project.movie_reservation_system.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "user-service", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {
    @GetMapping("/api/v1/users/{userId}")
    UserDto getUserById(@PathVariable Long userId);
}
