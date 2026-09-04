package com.weeklyreport.backend.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.weeklyreport.backend.dto.ReportSummaryDto;
import com.weeklyreport.backend.entity.enums.ReportStatus;
import com.weeklyreport.backend.service.ReportService;

import lombok.RequiredArgsConstructor;

// Minimal manager-facing report list, just enough to find reports to review; full dashboard comes later.
@RestController
@RequestMapping("/api/manager/reports")
@RequiredArgsConstructor
public class ManagerReportController {

    private final ReportService reportService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<Page<ReportSummaryDto>> list(
            Pageable pageable,
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(reportService.getManagerReports(pageable, status, projectId, userId));
    }
}
