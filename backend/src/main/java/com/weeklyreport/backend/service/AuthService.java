package com.weeklyreport.backend.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.weeklyreport.backend.dto.AuthResponseDto;
import com.weeklyreport.backend.dto.LoginRequestDto;
import com.weeklyreport.backend.dto.RegisterRequestDto;
import com.weeklyreport.backend.entity.Role;
import com.weeklyreport.backend.entity.User;
import com.weeklyreport.backend.exception.DuplicateResourceException;
import com.weeklyreport.backend.exception.ResourceNotFoundException;
import com.weeklyreport.backend.repository.RoleRepository;
import com.weeklyreport.backend.repository.UserRepository;
import com.weeklyreport.backend.security.CustomUserDetails;
import com.weeklyreport.backend.security.JwtProperties;
import com.weeklyreport.backend.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String DEFAULT_REGISTRATION_ROLE = "TEAM_MEMBER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public AuthResponseDto register(RegisterRequestDto request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email is already registered: " + request.email());
        }

        Role role = roleRepository.findByName(DEFAULT_REGISTRATION_ROLE)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + DEFAULT_REGISTRATION_ROLE));

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(role)
                .active(true)
                .build();
        user = userRepository.save(user);

        return buildAuthResponse(new CustomUserDetails(user));
    }

    public AuthResponseDto login(LoginRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.email()));

        return buildAuthResponse(new CustomUserDetails(user));
    }

    private AuthResponseDto buildAuthResponse(CustomUserDetails userDetails) {
        String token = jwtService.generateToken(userDetails);
        LocalDateTime expiresAt = LocalDateTime.now().plus(jwtProperties.getExpirationMs(), ChronoUnit.MILLIS);

        User user = userDetails.getUser();
        return new AuthResponseDto(
                token,
                "Bearer",
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().getName(),
                expiresAt);
    }
}
