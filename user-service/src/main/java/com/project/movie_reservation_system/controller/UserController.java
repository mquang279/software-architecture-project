package com.project.movie_reservation_system.controller;

import com.project.movie_reservation_system.dto.response.PaginationResponse;
import com.project.movie_reservation_system.dto.response.UserResponseDto;
import com.project.movie_reservation_system.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> currentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());

    }

    @GetMapping("")
    public ResponseEntity<PaginationResponse<UserResponseDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(
                userService.getAllUser(page, pageSize));
    }
}
