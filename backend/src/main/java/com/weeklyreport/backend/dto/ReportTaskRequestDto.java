package com.weeklyreport.backend.dto;

import com.weeklyreport.backend.entity.enums.TaskPriority;
import com.weeklyreport.backend.entity.enums.TaskStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ReportTaskRequestDto(
        @NotBlank String taskName,
        TaskPriority priority,
        @Min(0) @Max(100) Integer plannedPercentage,
        @Min(0) @Max(100) Integer actualPercentage,
        TaskStatus status,
        Double plannedHours,
        Double spentHours,
        String deliverable) {
}
