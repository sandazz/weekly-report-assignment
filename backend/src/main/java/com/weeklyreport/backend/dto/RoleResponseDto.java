package com.weeklyreport.backend.dto;

import java.time.LocalDateTime;

public record RoleResponseDto(
        Long id,
        String name,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
