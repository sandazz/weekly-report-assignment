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

import com.weeklyreport.backend.dto.ReportHourRequestDto;
import com.weeklyreport.backend.dto.ReportHourResponseDto;
import com.weeklyreport.backend.security.CustomUserDetails;
import com.weeklyreport.backend.service.ReportHourService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reports/{reportId}/hours")
@RequiredArgsConstructor
public class ReportHourController {

    private final ReportHourService reportHourService;

    @PostMapping
    @PreAuthorize("hasRole('TEAM_MEMBER')")
    public ResponseEntity<ReportHourResponseDto> add(
            @PathVariable Long reportId,
            @Valid @RequestBody ReportHourRequestDto request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reportHourService.addHour(reportId, request, currentUser));
    }

    @PutMapping("/{hourId}")
    @PreAuthorize("hasRole('TEAM_MEMBER')")
    public ResponseEntity<ReportHourResponseDto> update(
            @PathVariable Long reportId,
            @PathVariable Long hourId,
            @Valid @RequestBody ReportHourRequestDto request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(reportHourService.updateHour(reportId, hourId, request, currentUser));
    }

    @DeleteMapping("/{hourId}")
    @PreAuthorize("hasRole('TEAM_MEMBER')")
    public ResponseEntity<Void> delete(
            @PathVariable Long reportId,
            @PathVariable Long hourId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        reportHourService.deleteHour(reportId, hourId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TEAM_MEMBER','MANAGER','ADMIN')")
    public ResponseEntity<List<ReportHourResponseDto>> list(
            @PathVariable Long reportId, @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(reportHourService.listHours(reportId, currentUser));
    }
}
