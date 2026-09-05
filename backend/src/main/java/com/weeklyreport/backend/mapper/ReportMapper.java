package com.weeklyreport.backend.mapper;

import java.util.List;

import com.weeklyreport.backend.dto.ReportRequestDto;
import com.weeklyreport.backend.dto.ReportResponseDto;
import com.weeklyreport.backend.dto.ReportSummaryDto;
import com.weeklyreport.backend.entity.Report;

// Note: user/project associations are resolved and set by the service layer.
public final class ReportMapper {

    private ReportMapper() {
    }

    public static Report toEntity(ReportRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return Report.builder()
                .weekStart(dto.weekStart())
                .weekEnd(dto.weekEnd())
                .nextWeekPlan(dto.nextWeekPlan())
                .keyBlocker(dto.keyBlocker())
                .keyAchievement(dto.keyAchievement())
                .note(dto.note())
                .build();
    }

    public static ReportSummaryDto toSummaryDto(Report report) {
        if (report == null) {
            return null;
        }
        return new ReportSummaryDto(
                report.getId(),
                report.getWeekStart(),
                report.getWeekEnd(),
                report.getProject().getName(),
                report.getUser().getName(),
                report.getStatus(),
                report.getUpdatedAt());
    }

    public static ReportResponseDto toResponseDto(Report report) {
        if (report == null) {
            return null;
        }
        return new ReportResponseDto(
                report.getId(),
                UserMapper.toResponseDto(report.getUser()),
                ProjectMapper.toResponseDto(report.getProject()),
                report.getWeekStart(),
                report.getWeekEnd(),
                report.getStatus(),
                report.getNextWeekPlan(),
                report.getKeyBlocker(),
                report.getKeyAchievement(),
                report.getNote(),
                report.getTasks() == null ? List.of()
                        : report.getTasks().stream().map(ReportTaskMapper::toResponseDto).toList(),
                report.getBlockers() == null ? List.of()
                        : report.getBlockers().stream().map(ReportBlockerMapper::toResponseDto).toList(),
                report.getAchievements() == null ? List.of()
                        : report.getAchievements().stream().map(ReportAchievementMapper::toResponseDto).toList(),
                report.getHours() == null ? List.of()
                        : report.getHours().stream().map(ReportHourMapper::toResponseDto).toList(),
                report.getCreatedAt(),
                report.getUpdatedAt());
    }
}
