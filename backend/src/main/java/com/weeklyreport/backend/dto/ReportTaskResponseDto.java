package com.weeklyreport.backend.dto;

import java.time.LocalDateTime;

import com.weeklyreport.backend.entity.enums.TaskPriority;
import com.weeklyreport.backend.entity.enums.TaskStatus;

public record ReportTaskResponseDto(
        Long id,
        String taskName,
        TaskPriority priority,
        Integer plannedPercentage,
        Integer actualPercentage,
        TaskStatus status,
        Double plannedHours,
        Double spentHours,
        String deliverable,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
