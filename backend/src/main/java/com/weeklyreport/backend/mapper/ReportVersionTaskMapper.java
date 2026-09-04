package com.weeklyreport.backend.mapper;

import com.weeklyreport.backend.dto.ReportVersionTaskDto;
import com.weeklyreport.backend.entity.ReportVersionTask;

public final class ReportVersionTaskMapper {

    private ReportVersionTaskMapper() {
    }

    public static ReportVersionTaskDto toDto(ReportVersionTask task) {
        if (task == null) {
            return null;
        }
        return new ReportVersionTaskDto(
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
