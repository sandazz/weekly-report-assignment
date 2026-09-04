package com.weeklyreport.backend.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.weeklyreport.backend.dto.ReportRequestDto;
import com.weeklyreport.backend.dto.ReportResponseDto;
import com.weeklyreport.backend.dto.ReportSummaryDto;
import com.weeklyreport.backend.entity.enums.ReportStatus;
import com.weeklyreport.backend.security.CustomUserDetails;
import com.weeklyreport.backend.service.ReportService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @PreAuthorize("hasRole('TEAM_MEMBER')")
    public ResponseEntity<ReportResponseDto> create(
            @Valid @RequestBody ReportRequestDto request, @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.createReport(request, currentUser));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEAM_MEMBER')")
    public ResponseEntity<ReportResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody ReportRequestDto request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(reportService.updateReport(id, request, currentUser));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEAM_MEMBER','MANAGER','ADMIN')")
    public ResponseEntity<ReportResponseDto> getById(
            @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(reportService.getReportById(id, currentUser));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('TEAM_MEMBER')")
    public ResponseEntity<Page<ReportSummaryDto>> getMyReports(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Pageable pageable,
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) Long projectId) {
        return ResponseEntity.ok(reportService.getMyReports(currentUser, pageable, status, projectId));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('TEAM_MEMBER')")
    public ResponseEntity<ReportResponseDto> submit(
            @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(reportService.submitReport(id, currentUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEAM_MEMBER')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        reportService.deleteReport(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
