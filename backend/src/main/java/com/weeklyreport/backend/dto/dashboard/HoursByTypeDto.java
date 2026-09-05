package com.weeklyreport.backend.dto.dashboard;

import com.weeklyreport.backend.entity.enums.TaskType;

public record HoursByTypeDto(
        TaskType taskType,
        double totalHours) {
}
