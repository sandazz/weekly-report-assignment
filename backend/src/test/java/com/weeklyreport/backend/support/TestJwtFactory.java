package com.weeklyreport.backend.support;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.weeklyreport.backend.entity.User;
import com.weeklyreport.backend.security.CustomUserDetails;
import com.weeklyreport.backend.security.JwtProperties;
import com.weeklyreport.backend.security.JwtService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

// Mints tokens directly (no register/login HTTP roundtrip needed) so RBAC tests are fast to write
// and run. expiredTokenFor builds a token with a past expiration using the same signing secret,
// without needing a test-only hook in the production JwtService.
@Component
@RequiredArgsConstructor
public class TestJwtFactory {

    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public String tokenFor(User user) {
        return jwtService.generateToken(new CustomUserDetails(user));
    }

    public String expiredTokenFor(User user) {
        CustomUserDetails details = new CustomUserDetails(user);
        Date issuedAt = new Date(System.currentTimeMillis() - 120_000);
        Date expiredAt = new Date(System.currentTimeMillis() - 60_000);
        SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(details.getUsername())
                .claim("userId", details.getUserId())
                .claim("role", details.getRoleName())
                .issuedAt(issuedAt)
                .expiration(expiredAt)
                .signWith(key)
                .compact();
    }
}
