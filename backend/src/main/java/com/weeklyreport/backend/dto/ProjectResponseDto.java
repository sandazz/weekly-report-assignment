package com.weeklyreport.backend.dto;

import java.time.LocalDateTime;

public record ProjectResponseDto(
        Long id,
        String name,
        String description,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
