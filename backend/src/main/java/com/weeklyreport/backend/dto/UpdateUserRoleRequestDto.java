package com.weeklyreport.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRoleRequestDto(
        @NotBlank String roleName) {
}
