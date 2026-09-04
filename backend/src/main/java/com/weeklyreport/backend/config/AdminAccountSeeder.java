package com.weeklyreport.backend.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.weeklyreport.backend.entity.Role;
import com.weeklyreport.backend.entity.User;
import com.weeklyreport.backend.exception.ResourceNotFoundException;
import com.weeklyreport.backend.repository.RoleRepository;
import com.weeklyreport.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Creates a default ADMIN account on first startup, since there's no way to reach the
// admin-only role-update endpoint until at least one admin already exists.
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminAccountSeeder implements ApplicationRunner {

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@gmail.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin@123";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByRole_Name(ADMIN_ROLE)) {
            return;
        }
        if (userRepository.existsByEmail(DEFAULT_ADMIN_EMAIL)) {
            log.warn("Cannot seed default admin: email {} is already taken by a non-admin user.", DEFAULT_ADMIN_EMAIL);
            return;
        }

        Role adminRole = roleRepository.findByName(ADMIN_ROLE)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + ADMIN_ROLE));

        User admin = User.builder()
                .name("Admin")
                .email(DEFAULT_ADMIN_EMAIL)
                .password(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD))
                .role(adminRole)
                .active(true)
                .build();
        userRepository.save(admin);

        log.warn("No admin account existed - created default admin '{}' with a well-known dev-only "
                + "password. Log in and change it (or promote a real account and deactivate this one).",
                DEFAULT_ADMIN_EMAIL);
    }
}
