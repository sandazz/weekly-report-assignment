package com.weeklyreport.backend.dto;

import java.time.LocalDateTime;

public record UserSummaryDto(
        Long id,
        String name,
        String email,
        String roleName,
        boolean active,
        LocalDateTime createdAt) {
}
