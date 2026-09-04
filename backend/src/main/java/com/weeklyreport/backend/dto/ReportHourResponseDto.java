package com.weeklyreport.backend.dto;

import java.time.LocalDateTime;

import com.weeklyreport.backend.entity.enums.TaskType;

public record ReportHourResponseDto(
        Long id,
        TaskType taskType,
        Double hours,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
