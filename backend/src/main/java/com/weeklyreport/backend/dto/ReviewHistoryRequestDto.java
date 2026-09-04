package com.weeklyreport.backend.dto;

import com.weeklyreport.backend.entity.enums.ReviewAction;

import jakarta.validation.constraints.NotNull;

public record ReviewHistoryRequestDto(
        @NotNull Long reportId,
        Long reportVersionId,
        @NotNull Long reviewerId,
        @NotNull ReviewAction action,
        String comment) {
}
