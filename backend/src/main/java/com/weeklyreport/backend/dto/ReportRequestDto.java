package com.weeklyreport.backend.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record ReportRequestDto(
        @NotNull Long projectId,
        @NotNull LocalDate weekStart,
        @NotNull LocalDate weekEnd,
        String nextWeekPlan,
        String keyBlocker,
        String keyAchievement,
        String note) {
}
