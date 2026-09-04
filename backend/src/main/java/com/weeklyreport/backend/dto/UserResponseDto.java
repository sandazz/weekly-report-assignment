package com.weeklyreport.backend.dto;

import java.time.LocalDateTime;

public record UserResponseDto(
        Long id,
        String name,
        String email,
        RoleResponseDto role,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
