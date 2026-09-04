package com.weeklyreport.backend.mapper;

import com.weeklyreport.backend.dto.ReportVersionTaskRequestDto;
import com.weeklyreport.backend.dto.ReportVersionTaskResponseDto;
import com.weeklyreport.backend.entity.ReportVersionTask;

public final class ReportVersionTaskMapper {

    private ReportVersionTaskMapper() {
    }

    public static ReportVersionTask toEntity(ReportVersionTaskRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return ReportVersionTask.builder()
                .taskName(dto.taskName())
                .plannedPercentage(dto.plannedPercentage())
                .actualPercentage(dto.actualPercentage())
                .status(dto.status())
                .plannedHours(dto.plannedHours())
                .spentHours(dto.spentHours())
                .deliverable(dto.deliverable())
                .build();
    }

    public static ReportVersionTaskResponseDto toResponseDto(ReportVersionTask task) {
        if (task == null) {
            return null;
        }
        return new ReportVersionTaskResponseDto(
                task.getId(),
                task.getTaskName(),
                task.getPlannedPercentage(),
                task.getActualPercentage(),
                task.getStatus(),
                task.getPlannedHours(),
                task.getSpentHours(),
                task.getDeliverable());
    }
}
