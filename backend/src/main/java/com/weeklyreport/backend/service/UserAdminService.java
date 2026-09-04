package com.weeklyreport.backend.service;

import org.springframework.stereotype.Service;

import com.weeklyreport.backend.entity.Role;
import com.weeklyreport.backend.entity.User;
import com.weeklyreport.backend.exception.ResourceNotFoundException;
import com.weeklyreport.backend.exception.ValidationException;
import com.weeklyreport.backend.repository.RoleRepository;
import com.weeklyreport.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public void updateUserRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ValidationException("Unknown role: " + roleName));

        user.setRole(role);
        userRepository.save(user);
    }
}
