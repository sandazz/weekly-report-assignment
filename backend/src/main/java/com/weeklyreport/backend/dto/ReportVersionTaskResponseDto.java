package com.weeklyreport.backend.dto;

import com.weeklyreport.backend.entity.enums.TaskStatus;

public record ReportVersionTaskResponseDto(
        Long id,
        String taskName,
        Integer plannedPercentage,
        Integer actualPercentage,
        TaskStatus status,
        Double plannedHours,
        Double spentHours,
        String deliverable
) {
}
