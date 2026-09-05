package com.weeklyreport.backend.dto.dashboard;

import java.time.LocalDateTime;

// reportId lets the frontend link a feed item straight to /reports/{reportId}.
public record ActivityFeedItemDto(
        String type,
        String description,
        String actorName,
        LocalDateTime timestamp,
        Long reportId) {
}
