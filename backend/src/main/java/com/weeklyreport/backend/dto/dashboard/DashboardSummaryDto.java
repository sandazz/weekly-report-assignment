package com.weeklyreport.backend.dto.dashboard;

public record DashboardSummaryDto(
        long totalReportsThisWeek,
        double complianceRatePercent,
        long submittedCount,
        long pendingCount,
        long lateCount,
        long needsCorrectionCount,
        long openBlockersCount) {
}
