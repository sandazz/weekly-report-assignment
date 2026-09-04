package com.weeklyreport.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record RoleRequestDto(
        @NotBlank String name) {
}
