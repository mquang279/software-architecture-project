package com.project.auth_service.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.auth_service.client.UserService;
import com.project.auth_service.dto.entity.UserDTO;
import com.project.auth_service.dto.request.LoginRequest;
import com.project.auth_service.dto.request.RegistrationRequest;
import com.project.auth_service.dto.response.AuthResponse;
import com.project.auth_service.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {
        private final AuthenticationManager authenticationManager;
        private final AuthService authService;
        private final PasswordEncoder passwordEncoder;
        private final UserService userService;

        @Value("${refresh.token.expiration.time}")
        private long refreshTokenExpiration;

        @Value("${jwt.secret}")
        private String secretKey;

        public AuthController(AuthenticationManager authenticationManager,
                        AuthService authService, PasswordEncoder passwordEncoder,
                        UserService userService) {
                this.authenticationManager = authenticationManager;
                this.userService = userService;
                this.authService = authService;
                this.passwordEncoder = passwordEncoder;
        }

        @PostMapping("/login")
        public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                loginRequest.getEmail(), loginRequest.getPassword());
                Authentication authentication = authenticationManager.authenticate(authenticationToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                String email = loginRequest.getEmail();
                UserDTO userDTO = this.userService.getUserByEmail(email);

                String accessToken = this.authService.generateAccessToken(userDTO.getEmail(), userDTO);
                String refreshToken = this.authService.generateRefreshToken(userDTO.getEmail(), userDTO);

                userService.updateRefreshToken(userDTO.getId(), refreshToken);
                AuthResponse response = new AuthResponse(accessToken, userDTO);

                ResponseCookie cookies = ResponseCookie
                                .from("refresh_token", refreshToken)
                                .path("/")
                                .maxAge(refreshTokenExpiration)
                                .secure(true)
                                .httpOnly(true)
                                .build();

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, cookies.toString())
                                .body(response);
        }

        @PostMapping("/refresh")
        public ResponseEntity<AuthResponse> refreshToken(
                        @CookieValue(name = "refresh_token", required = false) String refreshToken) {
                if (refreshToken == null || refreshToken.isEmpty()) {
                        throw new RuntimeException("Refresh token not found.");
                }

                UserDTO userDTO = this.userService.getUserByRefreshToken(refreshToken);

                String accessToken = this.authService.generateAccessToken(userDTO.getEmail(), userDTO);
                String newRefreshToken = this.authService.generateRefreshToken(userDTO.getEmail(), userDTO);

                this.userService.updateRefreshToken(userDTO.getId(), newRefreshToken);

                AuthResponse response = new AuthResponse(accessToken, userDTO);

                ResponseCookie cookies = ResponseCookie
                                .from("refresh_token", newRefreshToken)
                                .path("/")
                                .maxAge(refreshTokenExpiration)
                                .secure(true)
                                .httpOnly(true)
                                .build();

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, cookies.toString())
                                .body(response);
        }

        @PostMapping("/logout")
        public ResponseEntity<Void> logout() {
                ResponseCookie clearCookie = ResponseCookie
                                .from("refresh_token", "")
                                .path("/")
                                .maxAge(0)
                                .secure(true)
                                .httpOnly(true)
                                .build();

                return ResponseEntity.noContent()
                                .header(HttpHeaders.SET_COOKIE, clearCookie.toString()).build();
        }

        @PostMapping("/register")
        public ResponseEntity<UserDTO> register(@RequestBody RegistrationRequest request) {
                String hashPassword = this.passwordEncoder.encode(request.getPassword());
                request.setPassword(hashPassword);
                UserDTO userDTO = this.userService.createUser(request);
                return ResponseEntity.ok(userDTO);
        }

        @GetMapping("/me")
        public ResponseEntity<UserDTO> getCurrentUserLogin() {
                String email = this.authService.getCurrentUserEmail();
                UserDTO userDTO = this.userService.getUserByEmail(email);
                return ResponseEntity.ok(userDTO);
        }

}
