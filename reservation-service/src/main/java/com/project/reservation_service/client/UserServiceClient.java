package com.project.reservation_service.client;

import com.project.reservation_service.client.fallback.UserServiceClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.project.reservation_service.dto.UserDto;

@FeignClient(name = "user-service", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {
    @GetMapping("/api/v1/users/{userId}")
    UserDto getUserById(@PathVariable Long userId);
}
