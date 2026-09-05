package com.weeklyreport.backend.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weeklyreport.backend.dto.dashboard.ActivityFeedItemDto;
import com.weeklyreport.backend.dto.dashboard.DashboardSummaryDto;
import com.weeklyreport.backend.dto.dashboard.HoursByTypeDto;
import com.weeklyreport.backend.dto.dashboard.MemberStatusDto;
import com.weeklyreport.backend.dto.dashboard.ProjectWorkloadDto;
import com.weeklyreport.backend.dto.dashboard.TaskTrendPointDto;
import com.weeklyreport.backend.entity.Report;
import com.weeklyreport.backend.entity.ReportVersion;
import com.weeklyreport.backend.entity.ReviewHistory;
import com.weeklyreport.backend.entity.User;
import com.weeklyreport.backend.entity.enums.ReportStatus;
import com.weeklyreport.backend.repository.ReportBlockerRepository;
import com.weeklyreport.backend.repository.ReportHourRepository;
import com.weeklyreport.backend.repository.ReportRepository;
import com.weeklyreport.backend.repository.ReportTaskRepository;
import com.weeklyreport.backend.repository.ReportTaskRepository.WeeklyCompletedCount;
import com.weeklyreport.backend.repository.ReportVersionRepository;
import com.weeklyreport.backend.repository.ReviewHistoryRepository;
import com.weeklyreport.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final String TEAM_MEMBER_ROLE = "TEAM_MEMBER";
    private static final List<ReportStatus> SUBMITTED_LIKE = List.of(
            ReportStatus.SUBMITTED, ReportStatus.NEEDS_CORRECTION, ReportStatus.APPROVED);
    private static final DateTimeFormatter WEEK_LABEL_FORMAT = DateTimeFormatter.ofPattern("MMM dd");

    private final ReportRepository reportRepository;
    private final ReportTaskRepository reportTaskRepository;
    private final ReportHourRepository reportHourRepository;
    private final ReportBlockerRepository reportBlockerRepository;
    private final ReviewHistoryRepository reviewHistoryRepository;
    private final ReportVersionRepository reportVersionRepository;
    private final UserRepository userRepository;

    public DashboardSummaryDto getSummary() {
        LocalDate weekStart = currentWeekMonday();
        LocalDate weekEnd = weekStart.plusDays(6);
        LocalDate today = LocalDate.now();

        long totalReportsThisWeek = reportRepository.countByWeekStartAndStatusIn(weekStart, SUBMITTED_LIKE);

        List<User> activeTeamMembers = userRepository.findByRole_NameAndActiveTrue(TEAM_MEMBER_ROLE);
        Map<Long, Report> reportsByUser = reportsByUser(weekStart);

        long submitted = 0;
        long pending = 0;
        long late = 0;
        for (User member : activeTeamMembers) {
            Report report = reportsByUser.get(member.getId());
            boolean hasSubmittedLike = report != null && SUBMITTED_LIKE.contains(report.getStatus());
            if (hasSubmittedLike) {
                submitted++;
            } else if (today.isAfter(weekEnd)) {
                late++;
            } else {
                pending++;
            }
        }

        double complianceRate = activeTeamMembers.isEmpty() ? 0.0 : (submitted * 100.0 / activeTeamMembers.size());
        long needsCorrectionCount = reportRepository.countByStatus(ReportStatus.NEEDS_CORRECTION);
        long openBlockersCount = reportBlockerRepository.countByReport_StatusNot(ReportStatus.APPROVED);

        return new DashboardSummaryDto(
                totalReportsThisWeek, complianceRate, submitted, pending, late, needsCorrectionCount,
                openBlockersCount);
    }

    public List<TaskTrendPointDto> getTaskTrend(int weeks, Long userId) {
        LocalDate currentWeek = currentWeekMonday();
        LocalDate fromWeek = currentWeek.minusWeeks(weeks - 1L);

        Map<LocalDate, Long> countsByWeek = reportTaskRepository.findWeeklyCompletedCounts(fromWeek, userId).stream()
                .collect(java.util.stream.Collectors.toMap(
                        WeeklyCompletedCount::getWeekStart, WeeklyCompletedCount::getCompletedCount));

        List<TaskTrendPointDto> points = new ArrayList<>();
        for (int i = 0; i < weeks; i++) {
            LocalDate week = fromWeek.plusWeeks(i);
            long count = countsByWeek.getOrDefault(week, 0L);
            points.add(new TaskTrendPointDto(week.format(WEEK_LABEL_FORMAT), count));
        }
        return points;
    }

    public List<MemberStatusDto> getMemberStatusForWeek(LocalDate weekStart) {
        LocalDate effectiveWeekStart = weekStart != null ? weekStart : currentWeekMonday();
        List<User> activeTeamMembers = userRepository.findByRole_NameAndActiveTrue(TEAM_MEMBER_ROLE);
        Map<Long, Report> reportsByUser = reportsByUser(effectiveWeekStart);

        return activeTeamMembers.stream()
                .map(member -> {
                    Report report = reportsByUser.get(member.getId());
                    String status = report != null ? report.getStatus().name() : "NOT_STARTED";
                    return new MemberStatusDto(member.getId(), member.getName(), status);
                })
                .toList();
    }

    public List<ProjectWorkloadDto> getWorkloadByProject(LocalDate weekStart, LocalDate weekEnd) {
        LocalDate[] range = effectiveRange(weekStart, weekEnd);
        return reportTaskRepository.findWorkloadByProject(range[0], range[1]);
    }

    public List<HoursByTypeDto> getHoursByType(LocalDate weekStart, LocalDate weekEnd) {
        LocalDate[] range = effectiveRange(weekStart, weekEnd);
        return reportHourRepository.findHoursByType(range[0], range[1]);
    }

    public List<ActivityFeedItemDto> getActivityFeed(int limit) {
        List<ActivityFeedItemDto> reviews = reviewHistoryRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit))
                .stream()
                .map(this::toActivityItem)
                .toList();

        List<ActivityFeedItemDto> submissions = reportVersionRepository
                .findAllByOrderBySubmittedAtDesc(PageRequest.of(0, limit))
                .stream()
                .map(this::toActivityItem)
                .toList();

        return java.util.stream.Stream.concat(reviews.stream(), submissions.stream())
                .sorted(Comparator.comparing(ActivityFeedItemDto::timestamp).reversed())
                .limit(limit)
                .toList();
    }

    private ActivityFeedItemDto toActivityItem(ReviewHistory history) {
        Report report = history.getReport();
        String ownerName = report.getUser().getName();
        String weekRange = weekRangeLabel(report);
        String verb = history.getAction() == com.weeklyreport.backend.entity.enums.ReviewAction.APPROVED
                ? "approved"
                : "requested changes on";
        String description = "%s %s %s's report (Week %s)".formatted(
                history.getReviewer().getName(), verb, ownerName, weekRange);
        return new ActivityFeedItemDto("REVIEW", description, history.getReviewer().getName(), history.getCreatedAt(),
                report.getId());
    }

    private ActivityFeedItemDto toActivityItem(ReportVersion version) {
        Report report = version.getReport();
        String ownerName = report.getUser().getName();
        String weekRange = weekRangeLabel(report);
        String description = "%s submitted a report (Week %s)".formatted(ownerName, weekRange);
        return new ActivityFeedItemDto("SUBMISSION", description, ownerName, version.getSubmittedAt(),
                report.getId());
    }

    private String weekRangeLabel(Report report) {
        return "%s\u2013%s".formatted(report.getWeekStart(), report.getWeekEnd());
    }

    private Map<Long, Report> reportsByUser(LocalDate weekStart) {
        return reportRepository.findByWeekStart(weekStart).stream()
                .collect(java.util.stream.Collectors.toMap(
                        r -> r.getUser().getId(), Function.identity(), (first, second) -> first));
    }

    private LocalDate[] effectiveRange(LocalDate weekStart, LocalDate weekEnd) {
        if (weekStart != null && weekEnd != null) {
            return new LocalDate[] { weekStart, weekEnd };
        }
        LocalDate start = currentWeekMonday();
        return new LocalDate[] { start, start.plusDays(6) };
    }

    private LocalDate currentWeekMonday() {
        return LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
}
