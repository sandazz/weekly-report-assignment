package com.weeklyreport.backend.dto;

import java.time.LocalDateTime;

import com.weeklyreport.backend.entity.enums.ReviewAction;

public record ReviewHistoryResponseDto(
        Long id,
        Long reportId,
        Long reportVersionId,
        UserResponseDto reviewer,
        ReviewAction action,
        String comment,
        LocalDateTime createdAt) {
}
