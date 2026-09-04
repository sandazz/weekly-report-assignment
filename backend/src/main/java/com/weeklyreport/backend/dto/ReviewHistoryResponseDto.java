package com.weeklyreport.backend.dto;

import java.time.LocalDateTime;

import com.weeklyreport.backend.entity.enums.ReviewAction;

public record ReviewHistoryResponseDto(
                Long id,
                String reviewerName,
                ReviewAction action,
                String comment,
                Integer versionNumber,
                LocalDateTime createdAt) {
}
