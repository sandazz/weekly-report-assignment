package com.weeklyreport.backend.service;

import java.time.LocalDate;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weeklyreport.backend.dto.ReportRequestDto;
import com.weeklyreport.backend.dto.ReportResponseDto;
import com.weeklyreport.backend.dto.ReportSummaryDto;
import com.weeklyreport.backend.entity.Project;
import com.weeklyreport.backend.entity.Report;
import com.weeklyreport.backend.entity.enums.ReportStatus;
import com.weeklyreport.backend.exception.DuplicateResourceException;
import com.weeklyreport.backend.exception.InvalidReportStateException;
import com.weeklyreport.backend.exception.ResourceNotFoundException;
import com.weeklyreport.backend.mapper.ReportMapper;
import com.weeklyreport.backend.repository.ProjectRepository;
import com.weeklyreport.backend.repository.ReportRepository;
import com.weeklyreport.backend.repository.ReportSpecifications;
import com.weeklyreport.backend.repository.UserRepository;
import com.weeklyreport.backend.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

    private static final Set<ReportStatus> EDITABLE_STATUSES = Set.of(ReportStatus.DRAFT,
            ReportStatus.NEEDS_CORRECTION);
    private static final Set<ReportStatus> DELETABLE_STATUSES = Set.of(ReportStatus.DRAFT);

    private final ReportRepository reportRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ReportAccessGuard accessGuard;

    @Transactional
    public ReportResponseDto createReport(ReportRequestDto request, CustomUserDetails currentUser) {
        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + request.projectId()));

        checkNoDuplicate(currentUser.getUserId(), request.projectId(), request.weekStart(), null);

        Report report = ReportMapper.toEntity(request);
        report.setUser(userRepository.getReferenceById(currentUser.getUserId()));
        report.setProject(project);
        report.setStatus(ReportStatus.DRAFT);

        return ReportMapper.toResponseDto(reportRepository.save(report));
    }

    @Transactional
    public ReportResponseDto updateReport(Long reportId, ReportRequestDto request, CustomUserDetails currentUser) {
        Report report = accessGuard.loadEditable(reportId, currentUser, EDITABLE_STATUSES);

        boolean projectOrWeekChanged = !report.getProject().getId().equals(request.projectId())
                || !report.getWeekStart().equals(request.weekStart());
        if (projectOrWeekChanged) {
            checkNoDuplicate(currentUser.getUserId(), request.projectId(), request.weekStart(), reportId);
        }

        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + request.projectId()));

        report.setProject(project);
        report.setWeekStart(request.weekStart());
        report.setWeekEnd(request.weekEnd());
        report.setNextWeekPlan(request.nextWeekPlan());
        report.setKeyBlocker(request.keyBlocker());
        report.setKeyAchievement(request.keyAchievement());
        report.setNote(request.note());

        return ReportMapper.toResponseDto(reportRepository.save(report));
    }

    @Transactional(readOnly = true)
    public ReportResponseDto getReportById(Long reportId, CustomUserDetails currentUser) {
        return ReportMapper.toResponseDto(accessGuard.loadViewable(reportId, currentUser));
    }

    @Transactional(readOnly = true)
    public Page<ReportSummaryDto> getMyReports(
            CustomUserDetails currentUser, Pageable pageable, ReportStatus statusFilter, Long projectId) {
        Specification<Report> spec = ReportSpecifications.belongsToUser(currentUser.getUserId());
        if (statusFilter != null) {
            spec = spec.and(ReportSpecifications.hasStatus(statusFilter));
        }
        if (projectId != null) {
            spec = spec.and(ReportSpecifications.hasProject(projectId));
        }
        return reportRepository.findAll(spec, pageable).map(ReportMapper::toSummaryDto);
    }

    @Transactional
    public ReportResponseDto submitReport(Long reportId, CustomUserDetails currentUser) {
        Report report = accessGuard.loadEditable(reportId, currentUser, EDITABLE_STATUSES);
        if (report.getTasks().isEmpty()) {
            throw new InvalidReportStateException("At least one task is required to submit a report");
        }
        report.setStatus(ReportStatus.SUBMITTED);
        return ReportMapper.toResponseDto(reportRepository.save(report));
    }

    @Transactional
    public void deleteReport(Long reportId, CustomUserDetails currentUser) {
        Report report = accessGuard.loadEditable(reportId, currentUser, DELETABLE_STATUSES);
        reportRepository.delete(report);
    }

    private void checkNoDuplicate(Long userId, Long projectId, LocalDate weekStart, Long excludeReportId) {
        reportRepository.findByUserIdAndProjectIdAndWeekStart(userId, projectId, weekStart)
                .filter(existing -> excludeReportId == null || !existing.getId().equals(excludeReportId))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "A report for this project and week already exists");
                });
    }
}
