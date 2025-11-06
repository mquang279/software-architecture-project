package com.project.auth_service.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.project.auth_service.client.UserService;
import com.project.auth_service.dto.entity.UserClaims;
import com.project.auth_service.dto.entity.UserDTO;
import com.project.auth_service.service.AuthService;

@Service
public class AuthServiceImpl implements AuthService {

    @Value("${access.token.expiration.time}")
    private long accessTokenExpiration;

    @Value("${refresh.token.expiration.time}")
    private long refreshTokenExpiration;

    @Value("${jwt.secret}")
    private String secretKey;

    private JwtEncoder jwtEncoder;

    private UserService userService;

    public AuthServiceImpl(JwtEncoder jwtEncoder, UserService userService) {
        this.jwtEncoder = jwtEncoder;
        this.userService = userService;
    }

    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS256;

    @Override
    public String generateAccessToken(String email, UserDTO user) {
        Instant now = Instant.now();
        Instant validationTime = now.plus(this.accessTokenExpiration, ChronoUnit.SECONDS);

        List<String> authorities = new ArrayList<>();
        authorities.add(userService.getUserByEmail(email).getRole().toString());

        UserClaims userClaims = UserClaims.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .id(user.getId())
                .build();

        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuedAt(now)
                .subject(email)
                .expiresAt(validationTime)
                .issuer(email)
                .claim("user", userClaims)
                .claim("authorities", authorities)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claimsSet)).getTokenValue();
    }

    @Override
    public String generateRefreshToken(String email, UserDTO user) {
        Instant now = Instant.now();
        Instant validity = now.plus(this.refreshTokenExpiration, ChronoUnit.SECONDS);

        UserClaims userClaims = UserClaims.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .id(user.getId()).build();

        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(email)
                .claim("user", userClaims)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claimsSet)).getTokenValue();
    }

    @Override
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
