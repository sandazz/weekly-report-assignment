package com.weeklyreport.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.weeklyreport.backend.entity.enums.ReportStatus;

public record ReportResponseDto(
        Long id,
        UserResponseDto user,
        ProjectResponseDto project,
        LocalDate weekStart,
        LocalDate weekEnd,
        ReportStatus status,
        String nextWeekPlan,
        String keyBlocker,
        String keyAchievement,
        String note,
        List<ReportTaskResponseDto> tasks,
        List<ReportBlockerResponseDto> blockers,
        List<ReportAchievementResponseDto> achievements,
        List<ReportHourResponseDto> hours,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
