package com.weeklyreport.backend.dto;

import com.weeklyreport.backend.entity.enums.TaskPriority;
import com.weeklyreport.backend.entity.enums.TaskStatus;

import jakarta.validation.constraints.NotBlank;

public record ReportTaskRequestDto(
        @NotBlank String taskName,
        TaskPriority priority,
        Integer plannedPercentage,
        Integer actualPercentage,
        TaskStatus status,
        Double plannedHours,
        Double spentHours,
        String deliverable) {
}
