package com.weeklyreport.backend.mapper;

import com.weeklyreport.backend.dto.ReviewHistoryRequestDto;
import com.weeklyreport.backend.dto.ReviewHistoryResponseDto;
import com.weeklyreport.backend.entity.ReviewHistory;

// Note: report/reportVersion/reviewer associations are resolved and set by the service layer.
public final class ReviewHistoryMapper {

    private ReviewHistoryMapper() {
    }

    public static ReviewHistory toEntity(ReviewHistoryRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return ReviewHistory.builder()
                .action(dto.action())
                .comment(dto.comment())
                .build();
    }

    public static ReviewHistoryResponseDto toResponseDto(ReviewHistory history) {
        if (history == null) {
            return null;
        }
        return new ReviewHistoryResponseDto(
                history.getId(),
                history.getReport() == null ? null : history.getReport().getId(),
                history.getReportVersion() == null ? null : history.getReportVersion().getId(),
                UserMapper.toResponseDto(history.getReviewer()),
                history.getAction(),
                history.getComment(),
                history.getCreatedAt());
    }
}
