package com.weeklyreport.backend.dto;

import java.time.LocalDateTime;

public record ReportAchievementResponseDto(
        Long id,
        String description,
        boolean isKeyAchievement,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
