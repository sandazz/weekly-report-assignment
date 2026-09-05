package com.weeklyreport.backend.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.weeklyreport.backend.dto.dashboard.ActivityFeedItemDto;
import com.weeklyreport.backend.dto.dashboard.DashboardSummaryDto;
import com.weeklyreport.backend.dto.dashboard.HoursByTypeDto;
import com.weeklyreport.backend.dto.dashboard.MemberStatusDto;
import com.weeklyreport.backend.dto.dashboard.ProjectWorkloadDto;
import com.weeklyreport.backend.dto.dashboard.TaskTrendPointDto;
import com.weeklyreport.backend.service.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/manager/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDto> summary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    @GetMapping("/task-trend")
    public ResponseEntity<List<TaskTrendPointDto>> taskTrend(
            @RequestParam(defaultValue = "8") int weeks,
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(dashboardService.getTaskTrend(weeks, userId));
    }

    @GetMapping("/member-status")
    public ResponseEntity<List<MemberStatusDto>> memberStatus(
            @RequestParam(required = false) LocalDate weekStart) {
        return ResponseEntity.ok(dashboardService.getMemberStatusForWeek(weekStart));
    }

    @GetMapping("/workload-by-project")
    public ResponseEntity<List<ProjectWorkloadDto>> workloadByProject(
            @RequestParam(required = false) LocalDate weekStart,
            @RequestParam(required = false) LocalDate weekEnd) {
        return ResponseEntity.ok(dashboardService.getWorkloadByProject(weekStart, weekEnd));
    }

    @GetMapping("/hours-by-type")
    public ResponseEntity<List<HoursByTypeDto>> hoursByType(
            @RequestParam(required = false) LocalDate weekStart,
            @RequestParam(required = false) LocalDate weekEnd) {
        return ResponseEntity.ok(dashboardService.getHoursByType(weekStart, weekEnd));
    }

    @GetMapping("/activity-feed")
    public ResponseEntity<List<ActivityFeedItemDto>> activityFeed(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(dashboardService.getActivityFeed(limit));
    }
}
