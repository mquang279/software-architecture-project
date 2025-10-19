package com.project.movie_reservation_system.service.impl;

import com.project.movie_reservation_system.dto.entity.UserDTO;
import com.project.movie_reservation_system.dto.response.PaginationResponse;
import com.project.movie_reservation_system.dto.response.UserResponseDto;
import com.project.movie_reservation_system.entity.User;
import com.project.movie_reservation_system.repository.UserRepository;
import com.project.movie_reservation_system.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.project.movie_reservation_system.constant.ExceptionMessages.USER_NOT_FOUND;

@Service
public class UserServiceImpl implements UserService {
        private final UserRepository userRepository;

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
                                                .build())
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
                                                .build())
                                .toList();

                return PaginationResponse.<UserResponseDto>builder()
                                .pageNumber(page)
                                .pageSize(pageSize)
                                .totalPages(userPage.getTotalPages())
                                .totalElements(userPage.getTotalElements())
                                .data(users)
                                .build();
        }

        @Override
        public UserDTO createUser(User user) {

        }

        @Override
        public UserDTO getUserById(Long id) {
                throw new UnsupportedOperationException("Unimplemented method 'getUserById'");
        }

        @Override
        public UserDTO updateUser(Long id, User user) {
                throw new UnsupportedOperationException("Unimplemented method 'updateUser'");
        }

        @Override
        public void deleteUser(Long id) {
                throw new UnsupportedOperationException("Unimplemented method 'deleteUser'");
        }

        @Override
        public UserDTO convertToDTO(User user) {
                throw new UnsupportedOperationException("Unimplemented method 'convertToDTO'");
        }
}
