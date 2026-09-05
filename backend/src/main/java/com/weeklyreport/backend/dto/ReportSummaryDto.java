package com.weeklyreport.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.weeklyreport.backend.entity.enums.ReportStatus;

// Lightweight shape for paginated report lists — avoids pulling every task/blocker/achievement/hour
// for every row just to render a list.
public record ReportSummaryDto(
                Long id,
                LocalDate weekStart,
                LocalDate weekEnd,
                String projectName,
                String userName,
                ReportStatus status,
                LocalDateTime updatedAt) {
}
