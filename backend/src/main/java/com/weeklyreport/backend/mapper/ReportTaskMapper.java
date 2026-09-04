package com.weeklyreport.backend.mapper;

import com.weeklyreport.backend.dto.ReportTaskRequestDto;
import com.weeklyreport.backend.dto.ReportTaskResponseDto;
import com.weeklyreport.backend.entity.ReportTask;

public final class ReportTaskMapper {

    private ReportTaskMapper() {
    }

    public static ReportTask toEntity(ReportTaskRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return ReportTask.builder()
                .taskName(dto.taskName())
                .priority(dto.priority())
                .plannedPercentage(dto.plannedPercentage())
                .actualPercentage(dto.actualPercentage())
                .status(dto.status())
                .plannedHours(dto.plannedHours())
                .spentHours(dto.spentHours())
                .deliverable(dto.deliverable())
                .build();
    }

    public static ReportTaskResponseDto toResponseDto(ReportTask task) {
        if (task == null) {
            return null;
        }
        return new ReportTaskResponseDto(
                task.getId(),
                task.getTaskName(),
                task.getPriority(),
                task.getPlannedPercentage(),
                task.getActualPercentage(),
                task.getStatus(),
                task.getPlannedHours(),
                task.getSpentHours(),
                task.getDeliverable(),
                task.getCreatedAt(),
                task.getUpdatedAt());
    }
}
