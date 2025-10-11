package com.project.movie_reservation_system.controller;

import com.project.movie_reservation_system.dto.ApiResponse;
import com.project.movie_reservation_system.dto.PaginationResponse;
import com.project.movie_reservation_system.dto.UserResponseDto;
import com.project.movie_reservation_system.enums.Role;
import com.project.movie_reservation_system.repository.UserRepository;
import com.project.movie_reservation_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.project.movie_reservation_system.constant.ExceptionMessages.USER_NOT_FOUND;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user/me")
    public ResponseEntity<UserResponseDto> currentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());

    }

    @Secured({"ROLE_SUPER_ADMIN"})
    @GetMapping("/all")
    public ResponseEntity<PaginationResponse<UserResponseDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return ResponseEntity.ok(
                userService.getAllUser(page, pageSize)
        );
    }

    @Secured({"ROLE_SUPER_ADMIN"})
    @PostMapping("/user/promote/{username}")
    public ResponseEntity<UserResponseDto> promoteUserToAdmin(@PathVariable String username) {
        return ResponseEntity.ok(userService.promoteUserToAdmin(username));
    }

}
