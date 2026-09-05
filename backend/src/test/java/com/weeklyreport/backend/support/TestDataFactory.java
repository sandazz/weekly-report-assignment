package com.weeklyreport.backend.support;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.weeklyreport.backend.dto.ReportAchievementRequestDto;
import com.weeklyreport.backend.dto.ReportBlockerRequestDto;
import com.weeklyreport.backend.dto.ReportHourRequestDto;
import com.weeklyreport.backend.dto.ReportRequestDto;
import com.weeklyreport.backend.dto.ReportTaskRequestDto;
import com.weeklyreport.backend.entity.Project;
import com.weeklyreport.backend.entity.Report;
import com.weeklyreport.backend.entity.Role;
import com.weeklyreport.backend.entity.User;
import com.weeklyreport.backend.entity.enums.ReportStatus;
import com.weeklyreport.backend.entity.enums.TaskPriority;
import com.weeklyreport.backend.entity.enums.TaskStatus;
import com.weeklyreport.backend.entity.enums.TaskType;
import com.weeklyreport.backend.repository.ProjectRepository;
import com.weeklyreport.backend.repository.ReportRepository;
import com.weeklyreport.backend.repository.RoleRepository;
import com.weeklyreport.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

// Test-only fixture builder: fast, deterministic user/project/report setup so test methods don't
// each hand-roll the same 10 lines. Flyway's role seed is skipped under the "test" profile, so
// roles are looked-up-or-created here instead.
@Component
@RequiredArgsConstructor
public class TestDataFactory {

    public static final String PASSWORD = "Password123!";

    private static final AtomicInteger COUNTER = new AtomicInteger();

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ReportRepository reportRepository;
    private final PasswordEncoder passwordEncoder;

    public User createTeamMember() {
        return createUser("TEAM_MEMBER");
    }

    public User createManager() {
        return createUser("MANAGER");
    }

    public User createAdmin() {
        return createUser("ADMIN");
    }

    private User createUser(String roleName) {
        Role role = getOrCreateRole(roleName);
        int n = COUNTER.incrementAndGet();
        User user = User.builder()
                .name(roleName + " Test User " + n)
                .email(roleName.toLowerCase() + n + "@test.example.com")
                .password(passwordEncoder.encode(PASSWORD))
                .role(role)
                .active(true)
                .build();
        return userRepository.save(user);
    }

    public Role getOrCreateRole(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(Role.builder().name(name).build()));
    }

    public Project createProject(String name) {
        return projectRepository.save(Project.builder()
                .name(name + "-" + COUNTER.incrementAndGet())
                .description("Test project")
                .active(true)
                .build());
    }

    public Report createDraftReport(User user, Project project, LocalDate weekStart) {
        Report report = Report.builder()
                .user(user)
                .project(project)
                .weekStart(weekStart)
                .weekEnd(weekStart.plusDays(6))
                .status(ReportStatus.DRAFT)
                .build();
        return reportRepository.save(report);
    }

    public Report createReportWithStatus(User user, Project project, LocalDate weekStart, ReportStatus status) {
        Report report = Report.builder()
                .user(user)
                .project(project)
                .weekStart(weekStart)
                .weekEnd(weekStart.plusDays(6))
                .status(status)
                .build();
        return reportRepository.save(report);
    }

    public ReportRequestDto reportRequest(Long projectId, LocalDate weekStart) {
        return new ReportRequestDto(
                projectId, weekStart, weekStart.plusDays(6), "Next week's plan", null, null, "Weekly note");
    }

    public ReportTaskRequestDto taskRequest(String name) {
        return new ReportTaskRequestDto(
                name, TaskPriority.MEDIUM, 50, 30, TaskStatus.IN_PROGRESS, 8.0, 5.0, "A deliverable");
    }

    public ReportBlockerRequestDto blockerRequest(String description, boolean keyIssue) {
        return new ReportBlockerRequestDto(description, keyIssue);
    }

    public ReportAchievementRequestDto achievementRequest(String description, boolean keyAchievement) {
        return new ReportAchievementRequestDto(description, keyAchievement);
    }

    public ReportHourRequestDto hourRequest(TaskType taskType, double hours) {
        return new ReportHourRequestDto(taskType, hours);
    }
}
