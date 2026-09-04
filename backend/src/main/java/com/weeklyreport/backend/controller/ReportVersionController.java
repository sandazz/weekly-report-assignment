package com.weeklyreport.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.weeklyreport.backend.dto.ReportVersionDetailDto;
import com.weeklyreport.backend.dto.ReportVersionSummaryDto;
import com.weeklyreport.backend.security.CustomUserDetails;
import com.weeklyreport.backend.service.ReportVersionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reports/{reportId}/versions")
@RequiredArgsConstructor
public class ReportVersionController {

    private final ReportVersionService reportVersionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('TEAM_MEMBER','MANAGER','ADMIN')")
    public ResponseEntity<List<ReportVersionSummaryDto>> list(
            @PathVariable Long reportId, @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(reportVersionService.getVersionsForReport(reportId, currentUser));
    }

    @GetMapping("/{versionId}")
    @PreAuthorize("hasAnyRole('TEAM_MEMBER','MANAGER','ADMIN')")
    public ResponseEntity<ReportVersionDetailDto> getDetail(
            @PathVariable Long reportId,
            @PathVariable Long versionId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(reportVersionService.getVersionDetail(reportId, versionId, currentUser));
    }
}
