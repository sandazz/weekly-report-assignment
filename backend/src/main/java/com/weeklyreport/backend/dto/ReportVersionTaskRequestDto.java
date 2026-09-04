package com.weeklyreport.backend.dto;

import com.weeklyreport.backend.entity.enums.TaskStatus;

import jakarta.validation.constraints.NotBlank;

public record ReportVersionTaskRequestDto(
        @NotBlank String taskName,
        Integer plannedPercentage,
        Integer actualPercentage,
        TaskStatus status,
        Double plannedHours,
        Double spentHours,
        String deliverable) {
}
