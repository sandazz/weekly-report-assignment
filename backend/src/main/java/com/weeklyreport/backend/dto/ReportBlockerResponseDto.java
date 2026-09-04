package com.weeklyreport.backend.dto;

import java.time.LocalDateTime;

public record ReportBlockerResponseDto(
        Long id,
        String description,
        boolean isKeyIssue,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
