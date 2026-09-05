package com.weeklyreport.backend.support;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.weeklyreport.backend.entity.Role;
import com.weeklyreport.backend.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

// Test-only substitute for production's Flyway-driven role seed migration (V2__seed_roles.sql),
// which never runs under the "test" profile (Flyway is disabled there in favor of H2 +
// Hibernate ddl-auto). Without this, any test path that touches a role by name before a
// TestDataFactory method happens to lazily create it (e.g. hitting /api/auth/register directly,
// or promoting a user to a role no fixture in that test ever created) would 404/400 on a
// nonexistent role row.
@Component
@Profile("test")
@RequiredArgsConstructor
public class TestRoleSeeder implements ApplicationRunner {

    private static final List<String> ROLE_NAMES = List.of("TEAM_MEMBER", "MANAGER", "ADMIN");

    private final RoleRepository roleRepository;

    @Override
    public void run(ApplicationArguments args) {
        for (String roleName : ROLE_NAMES) {
            roleRepository.findByName(roleName)
                    .orElseGet(() -> roleRepository.save(Role.builder().name(roleName).build()));
        }
    }
}
