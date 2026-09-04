package com.weeklyreport.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ReportVersionDetailDto(
                Long id,
                Integer versionNumber,
                String nextWeekPlan,
                String keyBlocker,
                String keyAchievement,
                String notes,
                LocalDateTime submittedAt,
                LocalDateTime createdAt,
                List<ReportVersionTaskDto> tasks) {
}
