package com.weeklyreport.backend.dto.dashboard;

public record MemberStatusDto(
        Long userId,
        String userName,
        String status) {
}
