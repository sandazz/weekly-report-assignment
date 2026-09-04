package com.weeklyreport.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record ProjectRequestDto(
        @NotBlank String name,
        String description,
        Boolean active) {
}
