package com.weeklyreport.backend.mapper;

import java.util.List;

import com.weeklyreport.backend.dto.ReportRequestDto;
import com.weeklyreport.backend.dto.ReportResponseDto;
import com.weeklyreport.backend.entity.Report;
import com.weeklyreport.backend.entity.ReportAchievement;
import com.weeklyreport.backend.entity.ReportBlocker;
import com.weeklyreport.backend.entity.ReportHour;
import com.weeklyreport.backend.entity.ReportTask;

// Note: user/project associations are resolved and set by the service layer.
public final class ReportMapper {

    private ReportMapper() {
    }

    public static Report toEntity(ReportRequestDto dto) {
        if (dto == null) {
            return null;
        }
        Report report = Report.builder()
                .weekStart(dto.weekStart())
                .weekEnd(dto.weekEnd())
                .status(dto.status())
                .nextWeekPlan(dto.nextWeekPlan())
                .keyBlocker(dto.keyBlocker())
                .keyAchievement(dto.keyAchievement())
                .note(dto.note())
                .build();

        if (dto.tasks() != null) {
            List<ReportTask> tasks = dto.tasks().stream()
                    .map(ReportTaskMapper::toEntity)
                    .peek(task -> task.setReport(report))
                    .toList();
            report.setTasks(tasks);
        }
        if (dto.blockers() != null) {
            List<ReportBlocker> blockers = dto.blockers().stream()
                    .map(ReportBlockerMapper::toEntity)
                    .peek(blocker -> blocker.setReport(report))
                    .toList();
            report.setBlockers(blockers);
        }
        if (dto.achievements() != null) {
            List<ReportAchievement> achievements = dto.achievements().stream()
                    .map(ReportAchievementMapper::toEntity)
                    .peek(achievement -> achievement.setReport(report))
                    .toList();
            report.setAchievements(achievements);
        }
        if (dto.hours() != null) {
            List<ReportHour> hours = dto.hours().stream()
                    .map(ReportHourMapper::toEntity)
                    .peek(hour -> hour.setReport(report))
                    .toList();
            report.setHours(hours);
        }
        return report;
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
                report.getTasks() == null ? List.of() : report.getTasks().stream().map(ReportTaskMapper::toResponseDto).toList(),
                report.getBlockers() == null ? List.of() : report.getBlockers().stream().map(ReportBlockerMapper::toResponseDto).toList(),
                report.getAchievements() == null ? List.of() : report.getAchievements().stream().map(ReportAchievementMapper::toResponseDto).toList(),
                report.getHours() == null ? List.of() : report.getHours().stream().map(ReportHourMapper::toResponseDto).toList(),
                report.getCreatedAt(),
                report.getUpdatedAt()
        );
    }
}
