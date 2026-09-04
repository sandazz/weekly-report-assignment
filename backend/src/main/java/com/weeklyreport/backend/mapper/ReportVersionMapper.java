package com.weeklyreport.backend.mapper;

import java.util.List;

import com.weeklyreport.backend.dto.ReportVersionRequestDto;
import com.weeklyreport.backend.dto.ReportVersionResponseDto;
import com.weeklyreport.backend.entity.ReportVersion;
import com.weeklyreport.backend.entity.ReportVersionTask;

// Note: the "report" association and back-references on child tasks are set by the service layer.
public final class ReportVersionMapper {

    private ReportVersionMapper() {
    }

    public static ReportVersion toEntity(ReportVersionRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return ReportVersion.builder()
                .versionNumber(dto.versionNumber())
                .nextWeekPlan(dto.nextWeekPlan())
                .keyBlocker(dto.keyBlocker())
                .keyAchievement(dto.keyAchievement())
                .notes(dto.notes())
                .submittedAt(dto.submittedAt())
                .build();
    }

    public static ReportVersionResponseDto toResponseDto(ReportVersion version) {
        if (version == null) {
            return null;
        }
        List<ReportVersionTask> tasks = version.getTasks();
        return new ReportVersionResponseDto(
                version.getId(),
                version.getVersionNumber(),
                version.getNextWeekPlan(),
                version.getKeyBlocker(),
                version.getKeyAchievement(),
                version.getNotes(),
                version.getSubmittedAt(),
                version.getCreatedAt(),
                tasks == null ? List.of() : tasks.stream().map(ReportVersionTaskMapper::toResponseDto).toList()
        );
    }
}
