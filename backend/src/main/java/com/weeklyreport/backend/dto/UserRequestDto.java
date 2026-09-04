package com.weeklyreport.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserRequestDto(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotNull Long roleId,
        Boolean active) {
}
