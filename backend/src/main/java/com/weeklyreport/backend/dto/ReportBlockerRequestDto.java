package com.weeklyreport.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record ReportBlockerRequestDto(
        @NotBlank String description,
        Boolean isKeyIssue
) {
}
