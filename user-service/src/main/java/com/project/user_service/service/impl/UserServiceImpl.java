package com.project.user_service.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.project.user_service.dto.entity.UserDTO;
import com.project.user_service.dto.request.CreateUserRequest;
import com.project.user_service.dto.request.RegistrationRequest;
import com.project.user_service.dto.request.UpdateUserRequest;
import com.project.user_service.dto.response.PaginationResponse;
import com.project.user_service.entity.User;
import com.project.user_service.exception.EmailAlreadyExistException;
import com.project.user_service.exception.UserNotFoundException;
import com.project.user_service.repository.UserRepository;
import com.project.user_service.service.UserService;

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
                Optional<User> userOptional = this.userRepository.findByEmail(request.getEmail());
                if (userOptional.isPresent()) {
                        throw new EmailAlreadyExistException();
                }
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
                                .username(user.getUsername())
                                .passwordHash(user.getPasswordHash())
                                .role(user.getRole())
                                .build();
        }

        @Override
        public UserDTO getUserByEmail(String email) {
                Optional<User> user = this.userRepository.findByEmail(email);
                if (user.isPresent()) {
                        return this.convertToDTO(user.get());
                } else {
                        throw new UserNotFoundException("User with this email is not existed");
                }
        }

        @Override
        public UserDTO createUser(RegistrationRequest request) {
                Optional<User> userOptional = this.userRepository.findByEmail(request.getEmail());
                if (userOptional.isPresent()) {
                        throw new EmailAlreadyExistException();
                }
                User user = User.builder()
                                .username(request.getUsername())
                                .email(request.getEmail())
                                .passwordHash(request.getPassword())
                                .build();
                this.userRepository.save(user);
                return this.convertToDTO(user);
        }

        @Override
        public UserDTO findByRefreshToken(String refreshToken) {
                Optional<User> user = this.userRepository.findByRefreshToken(refreshToken);
                if (user.isPresent()) {
                        return this.convertToDTO(user.get());
                } else {
                        throw new UserNotFoundException("User not found with refresh token");
                }
        }

        @Override
        public void updateRefreshToken(Long id, String refreshToken) {
                User user = this.userRepository.findById(id)
                                .orElseThrow(() -> new UserNotFoundException(id));
                user.setRefreshToken(refreshToken);
                this.userRepository.save(user);
        }
}
