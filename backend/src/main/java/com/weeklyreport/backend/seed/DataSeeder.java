package com.weeklyreport.backend.seed;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.weeklyreport.backend.dto.ReportAchievementRequestDto;
import com.weeklyreport.backend.dto.ReportBlockerRequestDto;
import com.weeklyreport.backend.dto.ReportHourRequestDto;
import com.weeklyreport.backend.dto.ReportRequestDto;
import com.weeklyreport.backend.dto.ReportResponseDto;
import com.weeklyreport.backend.dto.ReportTaskRequestDto;
import com.weeklyreport.backend.dto.ReviewActionRequestDto;
import com.weeklyreport.backend.entity.Project;
import com.weeklyreport.backend.entity.Role;
import com.weeklyreport.backend.entity.User;
import com.weeklyreport.backend.entity.enums.ReportStatus;
import com.weeklyreport.backend.entity.enums.ReviewAction;
import com.weeklyreport.backend.entity.enums.TaskPriority;
import com.weeklyreport.backend.entity.enums.TaskStatus;
import com.weeklyreport.backend.entity.enums.TaskType;
import com.weeklyreport.backend.repository.ProjectRepository;
import com.weeklyreport.backend.repository.ReportRepository;
import com.weeklyreport.backend.repository.RoleRepository;
import com.weeklyreport.backend.repository.ReviewHistoryRepository;
import com.weeklyreport.backend.repository.UserRepository;
import com.weeklyreport.backend.security.CustomUserDetails;
import com.weeklyreport.backend.service.ReportAchievementService;
import com.weeklyreport.backend.service.ReportBlockerService;
import com.weeklyreport.backend.service.ReportHourService;
import com.weeklyreport.backend.service.ReportService;
import com.weeklyreport.backend.service.ReportTaskService;
import com.weeklyreport.backend.service.ReviewService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Populates realistic demo data (dev/demo only) by driving the real create/submit/review service
// methods, so seeded reports get genuine ReportVersion/ReviewHistory rows exactly as real usage
// would produce. Admin already exists via AdminAccountSeeder; this only adds the manager + team.
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private static final String SEED_PASSWORD = "Password123!";
    private static final Random RANDOM = new Random(42);

    private static final String[] PROJECT_NAMES = { "Client A", "Internal Tooling", "R&D", "Marketing" };
    private static final String[] PROJECT_DESCRIPTIONS = {
            "Contracted engagement delivering features for our largest external client.",
            "Internal developer productivity and tooling improvements.",
            "Exploratory research and prototyping for future product bets.",
            "Website, campaigns, and content supporting the marketing team.",
    };

    private static final String[] TASK_NAMES = {
            "Implement login API", "Fix pagination bug on report list", "Write unit tests for ReportService",
            "Client A demo prep", "Investigate flaky CI test", "Refactor dashboard aggregation queries",
            "Design review for new onboarding flow", "Set up staging environment", "Clear code review backlog",
            "Update API documentation",
    };
    private static final TaskPriority[] PRIORITIES = TaskPriority.values();
    private static final TaskStatus[] TASK_STATUSES = TaskStatus.values();
    private static final TaskType[] TASK_TYPES = TaskType.values();

    private static final String[] BLOCKER_TEXTS = {
            "Waiting on API credentials from client", "Staging environment down since Tuesday",
            "Blocked on design review sign-off", "Dependency library has a breaking change to work around",
    };
    private static final String[] ACHIEVEMENT_TEXTS = {
            "Shipped the new dashboard to production", "Reduced test suite runtime by 40%",
            "Closed out the Q3 client review", "Onboarded successfully onto the new CI pipeline",
    };
    private static final String[] NOTES = {
            "Steady progress this week, no major surprises.",
            "Busy week juggling two projects, but stayed on track.",
            "Slower week due to some unplanned support work.",
            "Good momentum heading into next sprint.",
    };
    private static final String[] NEXT_WEEK_PLANS = {
            "Continue feature work and start the QA pass.",
            "Focus on closing out open code reviews and bug fixes.",
            "Kick off the next milestone and sync with stakeholders.",
            "Wrap up documentation and hand off to support.",
    };
    private static final String[] CORRECTION_COMMENTS = {
            "Please add more detail on the R&D blocker before I can approve this.",
            "Your hours don't add up to a full week, please double check.",
            "Can you clarify the deliverable for the second task?",
            "Missing a key achievement for this week - please add one.",
    };

    private record SeedUser(String name, String email) {
    }

    private static final SeedUser MANAGER = new SeedUser("Jordan Reyes", "manager.jordan@example.com");
    private static final SeedUser[] TEAM_MEMBERS = {
            new SeedUser("Alex Chen", "alex.chen@example.com"),
            new SeedUser("Priya Nair", "priya.nair@example.com"),
            new SeedUser("Sam Okafor", "sam.okafor@example.com"),
            new SeedUser("Morgan Lee", "morgan.lee@example.com"),
    };
    private static final int[] REPORT_COUNTS = { 4, 4, 3, 5 };

    private final ProjectRepository projectRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReportRepository reportRepository;
    private final ReviewHistoryRepository reviewHistoryRepository;
    private final ReportService reportService;
    private final ReportTaskService reportTaskService;
    private final ReportBlockerService reportBlockerService;
    private final ReportAchievementService reportAchievementService;
    private final ReportHourService reportHourService;
    private final ReviewService reviewService;

    private boolean forcedNeedsCorrectionUsed = false;

    @Override
    public void run(ApplicationArguments args) {
        if (projectRepository.existsByName(PROJECT_NAMES[0])) {
            log.info("Seed data already present (found project '{}') - skipping.", PROJECT_NAMES[0]);
            return;
        }

        Role teamMemberRole = getRole("TEAM_MEMBER");
        Role managerRole = getRole("MANAGER");

        List<Project> projects = createProjects();
        User manager = createUser(MANAGER.name(), MANAGER.email(), managerRole);
        List<User> teamMembers = new ArrayList<>();
        for (SeedUser seedUser : TEAM_MEMBERS) {
            teamMembers.add(createUser(seedUser.name(), seedUser.email(), teamMemberRole));
        }

        CustomUserDetails managerDetails = new CustomUserDetails(manager);
        LocalDate currentWeekMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        int recipeCounter = 0;
        for (int i = 0; i < teamMembers.size(); i++) {
            User member = teamMembers.get(i);
            CustomUserDetails memberDetails = new CustomUserDetails(member);
            int reportCount = REPORT_COUNTS[i];
            for (int w = reportCount; w >= 1; w--) {
                LocalDate weekStart = currentWeekMonday.minusWeeks(w);
                LocalDate weekEnd = weekStart.plusDays(6);
                Project project = projects.get(RANDOM.nextInt(projects.size()));

                int recipe;
                if (!forcedNeedsCorrectionUsed) {
                    recipe = 3;
                    forcedNeedsCorrectionUsed = true;
                } else {
                    recipe = (recipeCounter % 5) + 1;
                }
                recipeCounter++;

                seedReport(memberDetails, managerDetails, project, weekStart, weekEnd, recipe);
            }
        }

        logSummary();
        logCredentials(manager, teamMembers);
    }

    private void seedReport(
            CustomUserDetails memberDetails, CustomUserDetails managerDetails, Project project,
            LocalDate weekStart, LocalDate weekEnd, int recipe) {
        List<ReportBlockerRequestDto> blockerDtos = randomBlockers();
        List<ReportAchievementRequestDto> achievementDtos = randomAchievements();

        String keyBlockerText = blockerDtos.stream()
                .filter(b -> Boolean.TRUE.equals(b.isKeyIssue()))
                .findFirst().map(ReportBlockerRequestDto::description).orElse(null);
        String keyAchievementText = achievementDtos.stream()
                .filter(a -> Boolean.TRUE.equals(a.isKeyAchievement()))
                .findFirst().map(ReportAchievementRequestDto::description).orElse(null);

        ReportRequestDto createDto = new ReportRequestDto(
                project.getId(), weekStart, weekEnd, randomPick(NEXT_WEEK_PLANS), keyBlockerText,
                keyAchievementText, randomPick(NOTES));
        ReportResponseDto created = reportService.createReport(createDto, memberDetails);
        Long reportId = created.id();

        for (ReportTaskRequestDto dto : randomTasks()) {
            reportTaskService.addTask(reportId, dto, memberDetails);
        }
        for (ReportBlockerRequestDto dto : blockerDtos) {
            reportBlockerService.addBlocker(reportId, dto, memberDetails);
        }
        for (ReportAchievementRequestDto dto : achievementDtos) {
            reportAchievementService.addAchievement(reportId, dto, memberDetails);
        }
        for (ReportHourRequestDto dto : randomHours()) {
            reportHourService.addHour(reportId, dto, memberDetails);
        }

        if (recipe == 1) {
            return; // stays DRAFT
        }

        reportService.submitReport(reportId, memberDetails);
        if (recipe == 2) {
            return; // SUBMITTED, first time, awaiting review
        }

        String comment = randomPick(CORRECTION_COMMENTS);
        reviewService.reviewReport(reportId,
                new ReviewActionRequestDto(ReviewAction.REQUESTED_CHANGES, comment), managerDetails);
        if (recipe == 3) {
            return; // NEEDS_CORRECTION, not yet resubmitted
        }

        // A realistic edit addressing the manager's feedback before resubmitting.
        reportTaskService.addTask(reportId, new ReportTaskRequestDto(
                "Address manager feedback", TaskPriority.MEDIUM, 100, 100, TaskStatus.COMPLETED,
                2.0, 2.0, "Updated per manager review comment"), memberDetails);
        reportService.submitReport(reportId, memberDetails);
        if (recipe == 4) {
            return; // SUBMITTED again, awaiting re-review
        }

        reviewService.reviewReport(reportId,
                new ReviewActionRequestDto(ReviewAction.APPROVED, "Looks good, thanks for the updates."),
                managerDetails);
        // recipe == 5: APPROVED, full multi-version history
    }

    private List<Project> createProjects() {
        List<Project> projects = new ArrayList<>();
        for (int i = 0; i < PROJECT_NAMES.length; i++) {
            Project project = Project.builder()
                    .name(PROJECT_NAMES[i])
                    .description(PROJECT_DESCRIPTIONS[i])
                    .active(true)
                    .build();
            projects.add(projectRepository.save(project));
        }
        return projects;
    }

    private User createUser(String name, String email, Role role) {
        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(SEED_PASSWORD))
                .role(role)
                .active(true)
                .build();
        return userRepository.save(user);
    }

    private Role getRole(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException("Role not found: " + name));
    }

    private List<ReportTaskRequestDto> randomTasks() {
        int count = 2 + RANDOM.nextInt(3);
        Set<String> used = new HashSet<>();
        List<ReportTaskRequestDto> tasks = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String name;
            do {
                name = TASK_NAMES[RANDOM.nextInt(TASK_NAMES.length)];
            } while (!used.add(name) && used.size() < TASK_NAMES.length);
            TaskPriority priority = PRIORITIES[RANDOM.nextInt(PRIORITIES.length)];
            int planned = 20 + RANDOM.nextInt(9) * 10;
            int actual = Math.max(0, planned - RANDOM.nextInt(30));
            TaskStatus status = TASK_STATUSES[RANDOM.nextInt(TASK_STATUSES.length)];
            double plannedHours = 4 + RANDOM.nextInt(13);
            double spentHours = Math.max(1, plannedHours - RANDOM.nextInt(5));
            tasks.add(new ReportTaskRequestDto(
                    name, priority, planned, actual, status, plannedHours, spentHours,
                    "Deliverable for " + name.toLowerCase()));
        }
        return tasks;
    }

    private List<ReportBlockerRequestDto> randomBlockers() {
        int count = 1 + RANDOM.nextInt(2);
        int keyIndex = RANDOM.nextInt(count);
        List<ReportBlockerRequestDto> blockers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            blockers.add(new ReportBlockerRequestDto(randomPick(BLOCKER_TEXTS), i == keyIndex));
        }
        return blockers;
    }

    private List<ReportAchievementRequestDto> randomAchievements() {
        int count = 1 + RANDOM.nextInt(2);
        int keyIndex = RANDOM.nextInt(count);
        List<ReportAchievementRequestDto> achievements = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            achievements.add(new ReportAchievementRequestDto(randomPick(ACHIEVEMENT_TEXTS), i == keyIndex));
        }
        return achievements;
    }

    private List<ReportHourRequestDto> randomHours() {
        int count = 2 + RANDOM.nextInt(3);
        int remaining = 30 + RANDOM.nextInt(16);
        List<TaskType> types = new ArrayList<>(List.of(TASK_TYPES));
        Collections.shuffle(types, RANDOM);
        List<ReportHourRequestDto> hours = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int share = (i == count - 1) ? remaining : Math.max(1, remaining / (count - i));
            hours.add(new ReportHourRequestDto(types.get(i % types.size()), (double) share));
            remaining -= share;
        }
        return hours;
    }

    private String randomPick(String[] pool) {
        return pool[RANDOM.nextInt(pool.length)];
    }

    private void logSummary() {
        StringBuilder byStatus = new StringBuilder();
        long totalReports = 0;
        for (ReportStatus status : ReportStatus.values()) {
            long count = reportRepository.countByStatus(status);
            totalReports += count;
            byStatus.append(status).append('=').append(count).append(' ');
        }
        log.info("=== Seed data summary ===");
        log.info("Users: {} | Projects: {} | Reports: {} ({}) | Review history entries: {}",
                userRepository.count(), projectRepository.count(), totalReports,
                byStatus.toString().trim(), reviewHistoryRepository.count());
    }

    private void logCredentials(User manager, List<User> teamMembers) {
        log.info("=== Demo credentials (all newly seeded accounts share one password) ===");
        log.info("Admin (pre-existing, seeded by AdminAccountSeeder): admin@gmail.com / admin@123");
        log.info("Manager: {} / {}", manager.getEmail(), SEED_PASSWORD);
        for (User member : teamMembers) {
            log.info("Team member: {} / {}", member.getEmail(), SEED_PASSWORD);
        }
    }
}
