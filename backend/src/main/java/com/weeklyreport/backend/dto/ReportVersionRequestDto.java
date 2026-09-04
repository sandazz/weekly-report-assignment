package com.weeklyreport.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotNull;

public record ReportVersionRequestDto(
        @NotNull Integer versionNumber,
        String nextWeekPlan,
        String keyBlocker,
        String keyAchievement,
        String notes,
        LocalDateTime submittedAt,
        List<ReportVersionTaskRequestDto> tasks
) {
}
