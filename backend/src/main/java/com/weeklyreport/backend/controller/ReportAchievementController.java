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

import com.weeklyreport.backend.dto.ReportAchievementRequestDto;
import com.weeklyreport.backend.dto.ReportAchievementResponseDto;
import com.weeklyreport.backend.security.CustomUserDetails;
import com.weeklyreport.backend.service.ReportAchievementService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reports/{reportId}/achievements")
@RequiredArgsConstructor
public class ReportAchievementController {

    private final ReportAchievementService reportAchievementService;

    @PostMapping
    @PreAuthorize("hasRole('TEAM_MEMBER')")
    public ResponseEntity<ReportAchievementResponseDto> add(
            @PathVariable Long reportId,
            @Valid @RequestBody ReportAchievementRequestDto request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reportAchievementService.addAchievement(reportId, request, currentUser));
    }

    @PutMapping("/{achievementId}")
    @PreAuthorize("hasRole('TEAM_MEMBER')")
    public ResponseEntity<ReportAchievementResponseDto> update(
            @PathVariable Long reportId,
            @PathVariable Long achievementId,
            @Valid @RequestBody ReportAchievementRequestDto request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(
                reportAchievementService.updateAchievement(reportId, achievementId, request, currentUser));
    }

    @DeleteMapping("/{achievementId}")
    @PreAuthorize("hasRole('TEAM_MEMBER')")
    public ResponseEntity<Void> delete(
            @PathVariable Long reportId,
            @PathVariable Long achievementId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        reportAchievementService.deleteAchievement(reportId, achievementId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TEAM_MEMBER','MANAGER','ADMIN')")
    public ResponseEntity<List<ReportAchievementResponseDto>> list(
            @PathVariable Long reportId, @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(reportAchievementService.listAchievements(reportId, currentUser));
    }
}
