package com.weeklyreport.backend.dto;

import java.time.LocalDateTime;

public record ReportVersionSummaryDto(
        Long id,
        Integer versionNumber,
        LocalDateTime submittedAt) {
}
