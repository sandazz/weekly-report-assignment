package com.weeklyreport.backend.dto.dashboard;

public record ProjectWorkloadDto(
        Long projectId,
        String projectName,
        long taskCount,
        double totalPlannedHours) {
}
