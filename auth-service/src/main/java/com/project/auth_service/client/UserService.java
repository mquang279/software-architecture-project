package com.project.auth_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.auth_service.dto.entity.UserDTO;
import com.project.auth_service.dto.request.RegistrationRequest;

@FeignClient(name = "user-service", path = "/api/v1/users")
public interface UserService {

    @GetMapping("/email")
    UserDTO getUserByEmail(@RequestParam("email") String email);

    @GetMapping("/refresh-token/{token}")
    UserDTO getUserByRefreshToken(@PathVariable("token") String token);

    @PostMapping("/register")
    UserDTO createUser(@RequestBody RegistrationRequest request);

    @PutMapping("/{id}/refresh-token")
    void updateRefreshToken(@PathVariable("id") Long id, @RequestBody String token);
}
