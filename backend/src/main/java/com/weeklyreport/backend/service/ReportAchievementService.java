package com.weeklyreport.backend.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weeklyreport.backend.dto.ReportAchievementRequestDto;
import com.weeklyreport.backend.dto.ReportAchievementResponseDto;
import com.weeklyreport.backend.entity.Report;
import com.weeklyreport.backend.entity.ReportAchievement;
import com.weeklyreport.backend.entity.enums.ReportStatus;
import com.weeklyreport.backend.exception.ResourceNotFoundException;
import com.weeklyreport.backend.mapper.ReportAchievementMapper;
import com.weeklyreport.backend.repository.ReportAchievementRepository;
import com.weeklyreport.backend.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportAchievementService {

    private static final Set<ReportStatus> EDITABLE_STATUSES = Set.of(ReportStatus.DRAFT,
            ReportStatus.NEEDS_CORRECTION);

    private final ReportAchievementRepository reportAchievementRepository;
    private final ReportAccessGuard accessGuard;

    @Transactional
    public ReportAchievementResponseDto addAchievement(
            Long reportId, ReportAchievementRequestDto request, CustomUserDetails currentUser) {
        Report report = accessGuard.loadEditable(reportId, currentUser, EDITABLE_STATUSES);
        boolean isKeyAchievement = Boolean.TRUE.equals(request.isKeyAchievement());
        if (isKeyAchievement) {
            clearOtherKeyAchievements(reportId, null);
        }

        ReportAchievement achievement = ReportAchievementMapper.toEntity(request);
        achievement.setReport(report);
        return ReportAchievementMapper.toResponseDto(reportAchievementRepository.save(achievement));
    }

    @Transactional
    public ReportAchievementResponseDto updateAchievement(
            Long reportId, Long achievementId, ReportAchievementRequestDto request, CustomUserDetails currentUser) {
        accessGuard.loadEditable(reportId, currentUser, EDITABLE_STATUSES);
        ReportAchievement achievement = getAchievementInReport(reportId, achievementId);

        boolean isKeyAchievement = Boolean.TRUE.equals(request.isKeyAchievement());
        if (isKeyAchievement) {
            clearOtherKeyAchievements(reportId, achievementId);
        }

        achievement.setDescription(request.description());
        achievement.setKeyAchievement(isKeyAchievement);

        return ReportAchievementMapper.toResponseDto(reportAchievementRepository.save(achievement));
    }

    @Transactional
    public void deleteAchievement(Long reportId, Long achievementId, CustomUserDetails currentUser) {
        accessGuard.loadEditable(reportId, currentUser, EDITABLE_STATUSES);
        reportAchievementRepository.delete(getAchievementInReport(reportId, achievementId));
    }

    @Transactional(readOnly = true)
    public List<ReportAchievementResponseDto> listAchievements(Long reportId, CustomUserDetails currentUser) {
        accessGuard.loadViewable(reportId, currentUser);
        return reportAchievementRepository.findByReportId(reportId).stream()
                .map(ReportAchievementMapper::toResponseDto)
                .toList();
    }

    // Only one achievement per report may be flagged as the key achievement.
    private void clearOtherKeyAchievements(Long reportId, Long exceptAchievementId) {
        List<ReportAchievement> others = reportAchievementRepository.findByReportId(reportId).stream()
                .filter(a -> a.isKeyAchievement()
                        && (exceptAchievementId == null || !a.getId().equals(exceptAchievementId)))
                .toList();
        others.forEach(a -> a.setKeyAchievement(false));
        reportAchievementRepository.saveAll(others);
    }

    private ReportAchievement getAchievementInReport(Long reportId, Long achievementId) {
        ReportAchievement achievement = reportAchievementRepository.findById(achievementId)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found: " + achievementId));
        if (!achievement.getReport().getId().equals(reportId)) {
            throw new ResourceNotFoundException("Achievement not found: " + achievementId);
        }
        return achievement;
    }
}
