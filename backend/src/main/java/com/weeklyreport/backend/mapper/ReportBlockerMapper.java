package com.weeklyreport.backend.mapper;

import com.weeklyreport.backend.dto.ReportBlockerRequestDto;
import com.weeklyreport.backend.dto.ReportBlockerResponseDto;
import com.weeklyreport.backend.entity.ReportBlocker;

public final class ReportBlockerMapper {

    private ReportBlockerMapper() {
    }

    public static ReportBlocker toEntity(ReportBlockerRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return ReportBlocker.builder()
                .description(dto.description())
                .isKeyIssue(dto.isKeyIssue() != null && dto.isKeyIssue())
                .build();
    }

    public static ReportBlockerResponseDto toResponseDto(ReportBlocker blocker) {
        if (blocker == null) {
            return null;
        }
        return new ReportBlockerResponseDto(
                blocker.getId(),
                blocker.getDescription(),
                blocker.isKeyIssue(),
                blocker.getCreatedAt(),
                blocker.getUpdatedAt()
        );
    }
}
