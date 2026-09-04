package com.weeklyreport.backend.mapper;

import com.weeklyreport.backend.dto.ReportHourRequestDto;
import com.weeklyreport.backend.dto.ReportHourResponseDto;
import com.weeklyreport.backend.entity.ReportHour;

public final class ReportHourMapper {

    private ReportHourMapper() {
    }

    public static ReportHour toEntity(ReportHourRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return ReportHour.builder()
                .taskType(dto.taskType())
                .hours(dto.hours())
                .build();
    }

    public static ReportHourResponseDto toResponseDto(ReportHour hour) {
        if (hour == null) {
            return null;
        }
        return new ReportHourResponseDto(
                hour.getId(),
                hour.getTaskType(),
                hour.getHours(),
                hour.getCreatedAt(),
                hour.getUpdatedAt()
        );
    }
}
