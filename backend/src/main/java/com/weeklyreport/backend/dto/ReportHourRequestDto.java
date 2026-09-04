package com.weeklyreport.backend.dto;

import com.weeklyreport.backend.entity.enums.TaskType;

import jakarta.validation.constraints.NotNull;

public record ReportHourRequestDto(
        TaskType taskType,
        @NotNull Double hours) {
}
