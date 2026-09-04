package com.weeklyreport.backend.service;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.weeklyreport.backend.entity.Report;
import com.weeklyreport.backend.entity.enums.ReportStatus;
import com.weeklyreport.backend.exception.InvalidReportStateException;
import com.weeklyreport.backend.exception.ResourceNotFoundException;
import com.weeklyreport.backend.exception.UnauthorizedActionException;
import com.weeklyreport.backend.repository.ReportRepository;
import com.weeklyreport.backend.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

// Single place for the ownership + report-status rules every report (sub-)resource must enforce.
@Component
@RequiredArgsConstructor
public class ReportAccessGuard {

    private static final String TEAM_MEMBER_ROLE = "TEAM_MEMBER";

    private final ReportRepository reportRepository;

    public Report getOrThrow(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found: " + reportId));
    }

    // TEAM_MEMBER must own the report; MANAGER/ADMIN can view any report. No status
    // restriction.
    public Report loadViewable(Long reportId, CustomUserDetails currentUser) {
        Report report = getOrThrow(reportId);
        if (isTeamMember(currentUser) && !isOwner(report, currentUser)) {
            throw new UnauthorizedActionException("You do not have access to this report");
        }
        return report;
    }

    // TEAM_MEMBER + owner required (MANAGER/ADMIN can never edit report content in
    // this phase),
    // then the report's status must be one of the allowed states for the action
    // being performed.
    public Report loadEditable(Long reportId, CustomUserDetails currentUser, Set<ReportStatus> allowedStatuses) {
        Report report = getOrThrow(reportId);
        if (!isTeamMember(currentUser) || !isOwner(report, currentUser)) {
            throw new UnauthorizedActionException("You do not have permission to edit this report");
        }
        if (!allowedStatuses.contains(report.getStatus())) {
            throw new InvalidReportStateException(
                    "Report is in status " + report.getStatus() + " and cannot be modified this way");
        }
        return report;
    }

    private boolean isTeamMember(CustomUserDetails currentUser) {
        return TEAM_MEMBER_ROLE.equals(currentUser.getRoleName());
    }

    private boolean isOwner(Report report, CustomUserDetails currentUser) {
        return report.getUser().getId().equals(currentUser.getUserId());
    }
}
