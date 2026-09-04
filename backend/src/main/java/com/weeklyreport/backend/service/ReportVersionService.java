package com.weeklyreport.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weeklyreport.backend.dto.ReportVersionDetailDto;
import com.weeklyreport.backend.dto.ReportVersionSummaryDto;
import com.weeklyreport.backend.entity.ReportVersion;
import com.weeklyreport.backend.exception.ResourceNotFoundException;
import com.weeklyreport.backend.mapper.ReportVersionMapper;
import com.weeklyreport.backend.repository.ReportVersionRepository;
import com.weeklyreport.backend.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportVersionService {

    private final ReportVersionRepository reportVersionRepository;
    private final ReportAccessGuard accessGuard;

    @Transactional(readOnly = true)
    public List<ReportVersionSummaryDto> getVersionsForReport(Long reportId, CustomUserDetails currentUser) {
        accessGuard.loadViewable(reportId, currentUser);
        return reportVersionRepository.findByReportIdOrderByVersionNumberDesc(reportId).stream()
                .map(ReportVersionMapper::toSummaryDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReportVersionDetailDto getVersionDetail(Long reportId, Long versionId, CustomUserDetails currentUser) {
        accessGuard.loadViewable(reportId, currentUser);
        ReportVersion version = reportVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Version not found: " + versionId));
        if (!version.getReport().getId().equals(reportId)) {
            throw new ResourceNotFoundException("Version not found: " + versionId);
        }
        return ReportVersionMapper.toDetailDto(version);
    }
}
