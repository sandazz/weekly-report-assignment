package com.weeklyreport.backend.dto;

import java.time.LocalDateTime;

public record AuthResponseDto(
        String token,
        String tokenType,
        Long userId,
        String name,
        String email,
        String role,
        LocalDateTime expiresAt) {
}
