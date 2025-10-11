package com.project.movie_reservation_system.service.impl;

import com.project.movie_reservation_system.dto.PaginationResponse;
import com.project.movie_reservation_system.dto.UserResponseDto;
import com.project.movie_reservation_system.entity.Movie;
import com.project.movie_reservation_system.entity.User;
import com.project.movie_reservation_system.enums.Role;
import com.project.movie_reservation_system.repository.UserRepository;
import com.project.movie_reservation_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.project.movie_reservation_system.constant.ExceptionMessages.USER_NOT_FOUND;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponseDto getCurrentUser() {
        String currentUsername = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(currentUsername)
                .map(user -> UserResponseDto.builder()
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .role(user.getRole())
                        .username(user.getUsername())
                        .id(user.getId())
                        .build()
                )
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND));
    }

    public PaginationResponse<UserResponseDto> getAllUser(int page, int pageSize) {
        Page<User> userPage = userRepository.findAll(PageRequest.of(page, pageSize));
        List<UserResponseDto> users = userPage.getContent()
                .stream()
                .map(user -> UserResponseDto.builder()
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .role(user.getRole())
                        .username(user.getUsername())
                        .id(user.getId())
                        .build()
                )
                .toList();

        return PaginationResponse.<UserResponseDto>builder()
                .pageNumber(page)
                .pageSize(pageSize)
                .totalPages(userPage.getTotalPages())
                .totalElements(userPage.getTotalElements())
                .data(users)
                .build();
    }

    public UserResponseDto promoteUserToAdmin(String username) {
        return userRepository.findByUsername(username)
                .map(userInDb -> {
                    userInDb.setRole(Role.ROLE_ADMIN);
                    return userRepository.save(userInDb);
                })
                .map(updatedUser -> UserResponseDto.builder()
                        .email(updatedUser.getEmail())
                        .firstName(updatedUser.getFirstName())
                        .lastName(updatedUser.getLastName())
                        .role(updatedUser.getRole())
                        .username(updatedUser.getUsername())
                        .id(updatedUser.getId())
                        .build()
                )
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND));
    }


}
