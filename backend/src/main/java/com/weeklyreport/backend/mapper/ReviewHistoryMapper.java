package com.weeklyreport.backend.mapper;

import com.weeklyreport.backend.dto.ReviewHistoryResponseDto;
import com.weeklyreport.backend.entity.ReviewHistory;

public final class ReviewHistoryMapper {

    private ReviewHistoryMapper() {
    }

    public static ReviewHistoryResponseDto toResponseDto(ReviewHistory history) {
        if (history == null) {
            return null;
        }
        return new ReviewHistoryResponseDto(
                history.getId(),
                history.getReviewer().getName(),
                history.getAction(),
                history.getComment(),
                history.getReportVersion() == null ? null : history.getReportVersion().getVersionNumber(),
                history.getCreatedAt());
    }
}
