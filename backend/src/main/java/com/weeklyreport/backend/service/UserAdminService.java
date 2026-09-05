package com.weeklyreport.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weeklyreport.backend.dto.UserSummaryDto;
import com.weeklyreport.backend.entity.Role;
import com.weeklyreport.backend.entity.User;
import com.weeklyreport.backend.exception.ResourceNotFoundException;
import com.weeklyreport.backend.exception.ValidationException;
import com.weeklyreport.backend.mapper.UserMapper;
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

    @Transactional(readOnly = true)
    public Page<UserSummaryDto> listUsers(Pageable pageable, String roleNameFilter) {
        Page<User> users = roleNameFilter != null
                ? userRepository.findByRole_Name(roleNameFilter, pageable)
                : userRepository.findAll(pageable);
        return users.map(UserMapper::toSummaryDto);
    }

    public void updateUserActive(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setActive(active);
        userRepository.save(user);
    }
}
