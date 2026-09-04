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

import com.weeklyreport.backend.dto.ReportTaskRequestDto;
import com.weeklyreport.backend.dto.ReportTaskResponseDto;
import com.weeklyreport.backend.security.CustomUserDetails;
import com.weeklyreport.backend.service.ReportTaskService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reports/{reportId}/tasks")
@RequiredArgsConstructor
public class ReportTaskController {

    private final ReportTaskService reportTaskService;

    @PostMapping
    @PreAuthorize("hasRole('TEAM_MEMBER')")
    public ResponseEntity<ReportTaskResponseDto> add(
            @PathVariable Long reportId,
            @Valid @RequestBody ReportTaskRequestDto request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reportTaskService.addTask(reportId, request, currentUser));
    }

    @PutMapping("/{taskId}")
    @PreAuthorize("hasRole('TEAM_MEMBER')")
    public ResponseEntity<ReportTaskResponseDto> update(
            @PathVariable Long reportId,
            @PathVariable Long taskId,
            @Valid @RequestBody ReportTaskRequestDto request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(reportTaskService.updateTask(reportId, taskId, request, currentUser));
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasRole('TEAM_MEMBER')")
    public ResponseEntity<Void> delete(
            @PathVariable Long reportId, @PathVariable Long taskId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        reportTaskService.deleteTask(reportId, taskId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TEAM_MEMBER','MANAGER','ADMIN')")
    public ResponseEntity<List<ReportTaskResponseDto>> list(
            @PathVariable Long reportId, @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(reportTaskService.listTasks(reportId, currentUser));
    }
}
