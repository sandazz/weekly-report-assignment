package com.weeklyreport.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.weeklyreport.backend.dto.UpdateUserRoleRequestDto;
import com.weeklyreport.backend.service.UserAdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// Minimal admin user-management; full CRUD comes in a later phase.
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserAdminService userAdminService;

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateRole(@PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequestDto request) {
        userAdminService.updateUserRole(id, request.roleName());
        return ResponseEntity.noContent().build();
    }
}
