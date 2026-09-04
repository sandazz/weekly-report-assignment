package com.weeklyreport.backend.mapper;

import com.weeklyreport.backend.dto.ReportAchievementRequestDto;
import com.weeklyreport.backend.dto.ReportAchievementResponseDto;
import com.weeklyreport.backend.entity.ReportAchievement;

public final class ReportAchievementMapper {

    private ReportAchievementMapper() {
    }

    public static ReportAchievement toEntity(ReportAchievementRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return ReportAchievement.builder()
                .description(dto.description())
                .isKeyAchievement(dto.isKeyAchievement() != null && dto.isKeyAchievement())
                .build();
    }

    public static ReportAchievementResponseDto toResponseDto(ReportAchievement achievement) {
        if (achievement == null) {
            return null;
        }
        return new ReportAchievementResponseDto(
                achievement.getId(),
                achievement.getDescription(),
                achievement.isKeyAchievement(),
                achievement.getCreatedAt(),
                achievement.getUpdatedAt());
    }
}
