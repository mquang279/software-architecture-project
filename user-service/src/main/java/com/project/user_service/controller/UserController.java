package com.project.user_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.user_service.dto.entity.UserDTO;
import com.project.user_service.dto.request.CreateUserRequest;
import com.project.user_service.dto.request.RegistrationRequest;
import com.project.user_service.dto.request.UpdateUserRequest;
import com.project.user_service.dto.response.PaginationResponse;
import com.project.user_service.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("")
    public ResponseEntity<UserDTO> createUser(@RequestBody CreateUserRequest request) {
        UserDTO userDTO = this.userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody RegistrationRequest request) {
        UserDTO userDTO = this.userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
    }

    @GetMapping("")
    public ResponseEntity<PaginationResponse<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int pageSize) {
        PaginationResponse<UserDTO> response = this.userService.getAllUser(page, pageSize);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO userDTO = this.userService.getUserById(id);
        return ResponseEntity.ok().body(userDTO);
    }

    @GetMapping("/email")
    public ResponseEntity<UserDTO> getUserByEmail(@RequestParam String email) {
        UserDTO userDTO = this.userService.getUserByEmail(email);
        return ResponseEntity.ok().body(userDTO);
    }

    @GetMapping("/refresh-token/{token}")
    public ResponseEntity<UserDTO> getUserByRefreshToken(@PathVariable String token) {
        UserDTO userDTO = this.userService.findByRefreshToken(token);
        return ResponseEntity.ok().body(userDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        UserDTO userDTO = this.userService.updateUser(id, request);
        return ResponseEntity.ok().body(userDTO);
    }

    @PutMapping("/{id}/refresh-token")
    public ResponseEntity<Void> updateRefreshToken(@PathVariable Long id, @RequestBody String token) {
        this.userService.updateRefreshToken(id, token);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        this.userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
