package com.weeklyreport.backend.mapper;

import java.util.List;

import com.weeklyreport.backend.dto.ReportVersionDetailDto;
import com.weeklyreport.backend.dto.ReportVersionSummaryDto;
import com.weeklyreport.backend.entity.ReportVersion;
import com.weeklyreport.backend.entity.ReportVersionTask;

public final class ReportVersionMapper {

    private ReportVersionMapper() {
    }

    public static ReportVersionSummaryDto toSummaryDto(ReportVersion version) {
        if (version == null) {
            return null;
        }
        return new ReportVersionSummaryDto(version.getId(), version.getVersionNumber(), version.getSubmittedAt());
    }

    public static ReportVersionDetailDto toDetailDto(ReportVersion version) {
        if (version == null) {
            return null;
        }
        List<ReportVersionTask> tasks = version.getTasks();
        return new ReportVersionDetailDto(
                version.getId(),
                version.getVersionNumber(),
                version.getNextWeekPlan(),
                version.getKeyBlocker(),
                version.getKeyAchievement(),
                version.getNotes(),
                version.getSubmittedAt(),
                version.getCreatedAt(),
                tasks == null ? List.of() : tasks.stream().map(ReportVersionTaskMapper::toDto).toList());
    }
}
