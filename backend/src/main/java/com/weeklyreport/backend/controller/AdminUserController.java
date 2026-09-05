package com.weeklyreport.backend.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.weeklyreport.backend.dto.UpdateUserActiveRequestDto;
import com.weeklyreport.backend.dto.UpdateUserRoleRequestDto;
import com.weeklyreport.backend.dto.UserSummaryDto;
import com.weeklyreport.backend.service.UserAdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserAdminService userAdminService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserSummaryDto>> list(
            Pageable pageable, @RequestParam(required = false) String roleName) {
        return ResponseEntity.ok(userAdminService.listUsers(pageable, roleName));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateRole(@PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequestDto request) {
        userAdminService.updateUserRole(id, request.roleName());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateActive(@PathVariable Long id,
            @Valid @RequestBody UpdateUserActiveRequestDto request) {
        userAdminService.updateUserActive(id, request.active());
        return ResponseEntity.noContent().build();
    }
}
