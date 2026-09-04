package com.weeklyreport.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record ReportAchievementRequestDto(
        @NotBlank String description,
        Boolean isKeyAchievement
) {
}
