package com.weeklyreport.backend.controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

import com.weeklyreport.backend.dto.ReportBlockerRequestDto;
import com.weeklyreport.backend.dto.ReportBlockerResponseDto;
import com.weeklyreport.backend.security.CustomUserDetails;
import com.weeklyreport.backend.service.ReportBlockerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reports/{reportId}/blockers")
@RequiredArgsConstructor
public class ReportBlockerController {

    private final ReportBlockerService reportBlockerService;

    @PostMapping
    @PreAuthorize("hasRole('TEAM_MEMBER')")
    public ResponseEntity<ReportBlockerResponseDto> add(
            @PathVariable Long reportId,
            @Valid @RequestBody ReportBlockerRequestDto request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reportBlockerService.addBlocker(reportId, request, currentUser));
    }

    @PutMapping("/{blockerId}")
    @PreAuthorize("hasRole('TEAM_MEMBER')")
    public ResponseEntity<ReportBlockerResponseDto> update(
            @PathVariable Long reportId,
            @PathVariable Long blockerId,
            @Valid @RequestBody ReportBlockerRequestDto request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(reportBlockerService.updateBlocker(reportId, blockerId, request, currentUser));
    }

    @DeleteMapping("/{blockerId}")
    @PreAuthorize("hasRole('TEAM_MEMBER')")
    public ResponseEntity<Void> delete(
            @PathVariable Long reportId,
            @PathVariable Long blockerId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        reportBlockerService.deleteBlocker(reportId, blockerId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TEAM_MEMBER','MANAGER','ADMIN')")
    public ResponseEntity<List<ReportBlockerResponseDto>> list(
            @PathVariable Long reportId, @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(reportBlockerService.listBlockers(reportId, currentUser));
    }
}
