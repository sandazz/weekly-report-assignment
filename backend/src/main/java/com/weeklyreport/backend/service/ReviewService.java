package com.weeklyreport.backend.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weeklyreport.backend.dto.ReportResponseDto;
import com.weeklyreport.backend.dto.ReviewActionRequestDto;
import com.weeklyreport.backend.dto.ReviewHistoryResponseDto;
import com.weeklyreport.backend.entity.Report;
import com.weeklyreport.backend.entity.ReportVersion;
import com.weeklyreport.backend.entity.ReviewHistory;
import com.weeklyreport.backend.entity.enums.ReportStatus;
import com.weeklyreport.backend.entity.enums.ReviewAction;
import com.weeklyreport.backend.exception.InvalidReportStateException;
import com.weeklyreport.backend.exception.ResourceNotFoundException;
import com.weeklyreport.backend.exception.UnauthorizedActionException;
import com.weeklyreport.backend.exception.ValidationException;
import com.weeklyreport.backend.mapper.ReportMapper;
import com.weeklyreport.backend.mapper.ReviewHistoryMapper;
import com.weeklyreport.backend.repository.ReportRepository;
import com.weeklyreport.backend.repository.ReportVersionRepository;
import com.weeklyreport.backend.repository.ReviewHistoryRepository;
import com.weeklyreport.backend.repository.UserRepository;
import com.weeklyreport.backend.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final Set<String> REVIEWER_ROLES = Set.of("MANAGER", "ADMIN");

    private final ReportRepository reportRepository;
    private final ReportVersionRepository reportVersionRepository;
    private final ReviewHistoryRepository reviewHistoryRepository;
    private final UserRepository userRepository;
    private final ReportAccessGuard accessGuard;

    @Transactional
    public ReportResponseDto reviewReport(Long reportId, ReviewActionRequestDto request,
            CustomUserDetails currentManager) {
        // Defense-in-depth: @PreAuthorize already gates this at the controller.
        if (!REVIEWER_ROLES.contains(currentManager.getRoleName())) {
            throw new UnauthorizedActionException("Only a manager or admin can review a report");
        }

        Report report = accessGuard.loadViewable(reportId, currentManager);
        if (report.getStatus() != ReportStatus.SUBMITTED) {
            throw new InvalidReportStateException(
                    "Report is in status " + report.getStatus() + " and cannot be reviewed");
        }

        if (request.action() == ReviewAction.REQUESTED_CHANGES
                && (request.comment() == null || request.comment().isBlank())) {
            throw new ValidationException("Comment is required when requesting changes");
        }

        ReportVersion latestVersion = reportVersionRepository.findByReportIdOrderByVersionNumberDesc(reportId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No version found for report " + reportId + " - submit should always create one"));

        ReviewHistory history = ReviewHistory.builder()
                .report(report)
                .reportVersion(latestVersion)
                .reviewer(userRepository.getReferenceById(currentManager.getUserId()))
                .action(request.action())
                .comment(request.comment())
                .build();
        reviewHistoryRepository.save(history);

        report.setStatus(request.action() == ReviewAction.APPROVED
                ? ReportStatus.APPROVED
                : ReportStatus.NEEDS_CORRECTION);

        return ReportMapper.toResponseDto(reportRepository.save(report));
    }

    @Transactional(readOnly = true)
    public List<ReviewHistoryResponseDto> getReviewHistory(Long reportId, CustomUserDetails currentUser) {
        accessGuard.loadViewable(reportId, currentUser);
        return reviewHistoryRepository.findByReportIdOrderByCreatedAtAsc(reportId).stream()
                .map(ReviewHistoryMapper::toResponseDto)
                .toList();
    }
}
