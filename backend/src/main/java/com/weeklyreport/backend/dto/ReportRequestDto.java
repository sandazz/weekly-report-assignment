package com.weeklyreport.backend.dto;

import java.time.LocalDate;
import java.util.List;

import com.weeklyreport.backend.entity.enums.ReportStatus;

import jakarta.validation.constraints.NotNull;

public record ReportRequestDto(
        @NotNull Long userId,
        @NotNull Long projectId,
        @NotNull LocalDate weekStart,
        @NotNull LocalDate weekEnd,
        ReportStatus status,
        String nextWeekPlan,
        String keyBlocker,
        String keyAchievement,
        String note,
        List<ReportTaskRequestDto> tasks,
        List<ReportBlockerRequestDto> blockers,
        List<ReportAchievementRequestDto> achievements,
        List<ReportHourRequestDto> hours
) {
}
