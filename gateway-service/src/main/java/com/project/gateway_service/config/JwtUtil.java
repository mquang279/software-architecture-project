package com.project.gateway_service.config;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Component
public class JwtUtil {
    private static final String SECRET_KEY = "Ffbpo4qGLCKLYfbwV4a8mgmPYgNxoa8rorhEg6dn18s=";

    public Claims validateAndGetClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.getUrlDecoder().decode(SECRET_KEY);
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }
}
