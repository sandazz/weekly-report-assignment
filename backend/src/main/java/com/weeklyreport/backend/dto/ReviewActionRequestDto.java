package com.weeklyreport.backend.dto;

import com.weeklyreport.backend.entity.enums.ReviewAction;

import jakarta.validation.constraints.NotNull;

// comment is intentionally not @NotBlank here: it's only required when action = REQUESTED_CHANGES,
// which is validated in ReviewService based on the action, not via bean validation.
public record ReviewActionRequestDto(
        @NotNull ReviewAction action,
        String comment) {
}
