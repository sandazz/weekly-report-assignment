package com.weeklyreport.backend.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weeklyreport.backend.dto.ReportHourRequestDto;
import com.weeklyreport.backend.dto.ReportHourResponseDto;
import com.weeklyreport.backend.entity.Report;
import com.weeklyreport.backend.entity.ReportHour;
import com.weeklyreport.backend.entity.enums.ReportStatus;
import com.weeklyreport.backend.exception.ResourceNotFoundException;
import com.weeklyreport.backend.mapper.ReportHourMapper;
import com.weeklyreport.backend.repository.ReportHourRepository;
import com.weeklyreport.backend.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportHourService {

    private static final Set<ReportStatus> EDITABLE_STATUSES = Set.of(ReportStatus.DRAFT,
            ReportStatus.NEEDS_CORRECTION);

    private final ReportHourRepository reportHourRepository;
    private final ReportAccessGuard accessGuard;

    @Transactional
    public ReportHourResponseDto addHour(Long reportId, ReportHourRequestDto request, CustomUserDetails currentUser) {
        Report report = accessGuard.loadEditable(reportId, currentUser, EDITABLE_STATUSES);
        ReportHour hour = ReportHourMapper.toEntity(request);
        hour.setReport(report);
        return ReportHourMapper.toResponseDto(reportHourRepository.save(hour));
    }

    @Transactional
    public ReportHourResponseDto updateHour(
            Long reportId, Long hourId, ReportHourRequestDto request, CustomUserDetails currentUser) {
        accessGuard.loadEditable(reportId, currentUser, EDITABLE_STATUSES);
        ReportHour hour = getHourInReport(reportId, hourId);

        hour.setTaskType(request.taskType());
        hour.setHours(request.hours());

        return ReportHourMapper.toResponseDto(reportHourRepository.save(hour));
    }

    @Transactional
    public void deleteHour(Long reportId, Long hourId, CustomUserDetails currentUser) {
        accessGuard.loadEditable(reportId, currentUser, EDITABLE_STATUSES);
        reportHourRepository.delete(getHourInReport(reportId, hourId));
    }

    @Transactional(readOnly = true)
    public List<ReportHourResponseDto> listHours(Long reportId, CustomUserDetails currentUser) {
        accessGuard.loadViewable(reportId, currentUser);
        return reportHourRepository.findByReportId(reportId).stream().map(ReportHourMapper::toResponseDto).toList();
    }

    private ReportHour getHourInReport(Long reportId, Long hourId) {
        ReportHour hour = reportHourRepository.findById(hourId)
                .orElseThrow(() -> new ResourceNotFoundException("Hour entry not found: " + hourId));
        if (!hour.getReport().getId().equals(reportId)) {
            throw new ResourceNotFoundException("Hour entry not found: " + hourId);
        }
        return hour;
    }
}
