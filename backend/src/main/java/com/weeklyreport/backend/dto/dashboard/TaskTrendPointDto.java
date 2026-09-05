package com.weeklyreport.backend.dto.dashboard;

public record TaskTrendPointDto(
        String weekLabel,
        long completedCount) {
}
