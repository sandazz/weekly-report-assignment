package com.weeklyreport.backend.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateUserActiveRequestDto(
        @NotNull Boolean active) {
}
