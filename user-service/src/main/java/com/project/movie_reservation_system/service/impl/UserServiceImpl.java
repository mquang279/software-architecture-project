package com.project.movie_reservation_system.service.impl;

import com.project.movie_reservation_system.dto.entity.UserDTO;
import com.project.movie_reservation_system.dto.request.CreateUserRequest;
import com.project.movie_reservation_system.dto.request.UpdateUserRequest;
import com.project.movie_reservation_system.dto.response.PaginationResponse;
import com.project.movie_reservation_system.entity.User;
import com.project.movie_reservation_system.exception.UserNotFoundException;
import com.project.movie_reservation_system.repository.UserRepository;
import com.project.movie_reservation_system.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
        private final UserRepository userRepository;

        public UserServiceImpl(UserRepository userRepository) {
                this.userRepository = userRepository;
        }

        public PaginationResponse<UserDTO> getAllUser(int page, int pageSize) {
                Page<User> userPage = userRepository.findAll(PageRequest.of(page, pageSize));
                List<UserDTO> userDTOs = userPage.stream()
                                .map(user -> this.convertToDTO(user))
                                .toList();

                return new PaginationResponse<>(
                                userPage.getNumber(),
                                userPage.getSize(),
                                userPage.getTotalPages(),
                                userPage.getTotalElements(),
                                userDTOs);
        }

        @Override
        public UserDTO createUser(CreateUserRequest request) {
                User user = User.builder()
                                .firstName(request.getFirstName())
                                .lastName(request.getLastName())
                                .username(request.getUsername())
                                .email(request.getEmail()).build();
                this.userRepository.save(user);
                return this.convertToDTO(user);
        }

        @Override
        public UserDTO getUserById(Long id) {
                Optional<User> userOptional = this.userRepository.findById(id);
                if (userOptional.isPresent()) {
                        return this.convertToDTO(userOptional.get());
                } else {
                        throw new UserNotFoundException(id);
                }
        }

        @Override
        public UserDTO updateUser(Long id, UpdateUserRequest request) {
                User user = this.userRepository.findById(id).get();
                if (user == null) {
                        throw new UserNotFoundException(id);
                }

                if (request.getEmail() != null) {
                        user.setEmail(request.getEmail());
                }
                if (request.getFirstName() != null) {
                        user.setFirstName(request.getFirstName());
                }
                if (request.getLastName() != null) {
                        user.setLastName(request.getLastName());
                }
                if (request.getUsername() != null) {
                        user.setUsername(request.getUsername());
                }

                this.userRepository.save(user);
                return this.convertToDTO(user);
        }

        @Override
        public void deleteUser(Long id) {
                User user = this.userRepository.findById(id).get();
                if (user == null) {
                        throw new UserNotFoundException(id);
                }
                this.userRepository.delete(user);
        }

        @Override
        public UserDTO convertToDTO(User user) {
                return UserDTO.builder()
                                .email(user.getEmail())
                                .firstName(user.getFirstName())
                                .lastname(user.getLastName())
                                .id(user.getId())
                                .username(user.getUsername()).build();
        }
}
