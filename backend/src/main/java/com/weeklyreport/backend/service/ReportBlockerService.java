package com.weeklyreport.backend.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weeklyreport.backend.dto.ReportBlockerRequestDto;
import com.weeklyreport.backend.dto.ReportBlockerResponseDto;
import com.weeklyreport.backend.entity.Report;
import com.weeklyreport.backend.entity.ReportBlocker;
import com.weeklyreport.backend.entity.enums.ReportStatus;
import com.weeklyreport.backend.exception.ResourceNotFoundException;
import com.weeklyreport.backend.mapper.ReportBlockerMapper;
import com.weeklyreport.backend.repository.ReportBlockerRepository;
import com.weeklyreport.backend.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportBlockerService {

    private static final Set<ReportStatus> EDITABLE_STATUSES = Set.of(ReportStatus.DRAFT,
            ReportStatus.NEEDS_CORRECTION);

    private final ReportBlockerRepository reportBlockerRepository;
    private final ReportAccessGuard accessGuard;

    @Transactional
    public ReportBlockerResponseDto addBlocker(
            Long reportId, ReportBlockerRequestDto request, CustomUserDetails currentUser) {
        Report report = accessGuard.loadEditable(reportId, currentUser, EDITABLE_STATUSES);
        boolean isKeyIssue = Boolean.TRUE.equals(request.isKeyIssue());
        if (isKeyIssue) {
            clearOtherKeyIssues(reportId, null);
        }

        ReportBlocker blocker = ReportBlockerMapper.toEntity(request);
        blocker.setReport(report);
        return ReportBlockerMapper.toResponseDto(reportBlockerRepository.save(blocker));
    }

    @Transactional
    public ReportBlockerResponseDto updateBlocker(
            Long reportId, Long blockerId, ReportBlockerRequestDto request, CustomUserDetails currentUser) {
        accessGuard.loadEditable(reportId, currentUser, EDITABLE_STATUSES);
        ReportBlocker blocker = getBlockerInReport(reportId, blockerId);

        boolean isKeyIssue = Boolean.TRUE.equals(request.isKeyIssue());
        if (isKeyIssue) {
            clearOtherKeyIssues(reportId, blockerId);
        }

        blocker.setDescription(request.description());
        blocker.setKeyIssue(isKeyIssue);

        return ReportBlockerMapper.toResponseDto(reportBlockerRepository.save(blocker));
    }

    @Transactional
    public void deleteBlocker(Long reportId, Long blockerId, CustomUserDetails currentUser) {
        accessGuard.loadEditable(reportId, currentUser, EDITABLE_STATUSES);
        reportBlockerRepository.delete(getBlockerInReport(reportId, blockerId));
    }

    @Transactional(readOnly = true)
    public List<ReportBlockerResponseDto> listBlockers(Long reportId, CustomUserDetails currentUser) {
        accessGuard.loadViewable(reportId, currentUser);
        return reportBlockerRepository.findByReportId(reportId).stream()
                .map(ReportBlockerMapper::toResponseDto)
                .toList();
    }

    // Only one blocker per report may be flagged as the key issue.
    private void clearOtherKeyIssues(Long reportId, Long exceptBlockerId) {
        List<ReportBlocker> others = reportBlockerRepository.findByReportId(reportId).stream()
                .filter(b -> b.isKeyIssue() && (exceptBlockerId == null || !b.getId().equals(exceptBlockerId)))
                .toList();
        others.forEach(b -> b.setKeyIssue(false));
        reportBlockerRepository.saveAll(others);
    }

    private ReportBlocker getBlockerInReport(Long reportId, Long blockerId) {
        ReportBlocker blocker = reportBlockerRepository.findById(blockerId)
                .orElseThrow(() -> new ResourceNotFoundException("Blocker not found: " + blockerId));
        if (!blocker.getReport().getId().equals(reportId)) {
            throw new ResourceNotFoundException("Blocker not found: " + blockerId);
        }
        return blocker;
    }
}
