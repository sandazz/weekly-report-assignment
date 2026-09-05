package com.weeklyreport.backend.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.weeklyreport.backend.entity.Project;
import com.weeklyreport.backend.entity.ReportBlocker;
import com.weeklyreport.backend.entity.ReportVersion;
import com.weeklyreport.backend.entity.User;
import com.weeklyreport.backend.repository.ReportBlockerRepository;
import com.weeklyreport.backend.repository.ReportVersionRepository;
import com.weeklyreport.backend.support.BaseIntegrationTest;

class ReportWorkflowTest extends BaseIntegrationTest {

    @Autowired
    private ReportVersionRepository reportVersionRepository;

    @Autowired
    private ReportBlockerRepository reportBlockerRepository;

    @Test
    void submitCreatesVersionOneWithCorrectSnapshottedTaskData() throws Exception {
        User member = testDataFactory.createTeamMember();
        Project project = testDataFactory.createProject("Workflow-Submit");
        String token = "Bearer " + testJwtFactory.tokenFor(member);
        Long reportId = createDraftReportViaHttp(member, project, token);

        mockMvc.perform(post("/api/reports/{id}/tasks", reportId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testDataFactory.taskRequest("Implement feature X"))))
                .andExpect(status().isCreated());
        flushAndClear();

        mockMvc.perform(post("/api/reports/{id}/submit", reportId).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
        flushAndClear();

        List<ReportVersion> versions = reportVersionRepository.findByReportIdOrderByVersionNumberDesc(reportId);
        assertThat(versions).hasSize(1);
        ReportVersion version1 = versions.get(0);
        assertThat(version1.getVersionNumber()).isEqualTo(1);
        assertThat(version1.getTasks()).hasSize(1);
        assertThat(version1.getTasks().get(0).getTaskName()).isEqualTo("Implement feature X");
    }

    @Test
    void submittingWithZeroTasksIsRejected() throws Exception {
        User member = testDataFactory.createTeamMember();
        Project project = testDataFactory.createProject("Workflow-ZeroTasks");
        String token = "Bearer " + testJwtFactory.tokenFor(member);
        Long reportId = createDraftReportViaHttp(member, project, token);

        mockMvc.perform(post("/api/reports/{id}/submit", reportId).header("Authorization", token))
                .andExpect(status().isConflict());
    }

    @Test
    void editingAfterSubmitIsRejected() throws Exception {
        User member = testDataFactory.createTeamMember();
        Project project = testDataFactory.createProject("Workflow-EditAfterSubmit");
        String token = "Bearer " + testJwtFactory.tokenFor(member);
        Long reportId = createDraftReportViaHttp(member, project, token);
        addTask(reportId, token, "Some task");
        submit(reportId, token);

        mockMvc.perform(put("/api/reports/{id}", reportId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        testDataFactory.reportRequest(project.getId(), LocalDate.now().minusWeeks(1)))))
                .andExpect(status().isConflict());
    }

    @Test
    void submittingAnAlreadySubmittedReportIsRejected() throws Exception {
        User member = testDataFactory.createTeamMember();
        Project project = testDataFactory.createProject("Workflow-DoubleSubmit");
        String token = "Bearer " + testJwtFactory.tokenFor(member);
        Long reportId = createDraftReportViaHttp(member, project, token);
        addTask(reportId, token, "Some task");
        submit(reportId, token);

        mockMvc.perform(post("/api/reports/{id}/submit", reportId).header("Authorization", token))
                .andExpect(status().isConflict());
    }

    @Test
    void fullCorrectionCycleThenApprove() throws Exception {
        User member = testDataFactory.createTeamMember();
        User manager = testDataFactory.createManager();
        Project project = testDataFactory.createProject("Workflow-FullCycle");
        String memberToken = "Bearer " + testJwtFactory.tokenFor(member);
        String managerToken = "Bearer " + testJwtFactory.tokenFor(manager);

        Long reportId = createDraftReportViaHttp(member, project, memberToken);
        addTask(reportId, memberToken, "First task");
        submit(reportId, memberToken);

        // Missing comment on REQUESTED_CHANGES -> rejected
        mockMvc.perform(post("/api/reports/{id}/review", reportId)
                .header("Authorization", managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ReviewBody("REQUESTED_CHANGES", null))))
                .andExpect(status().isBadRequest());
        flushAndClear();

        // With a real comment -> accepted, status NEEDS_CORRECTION
        mockMvc.perform(post("/api/reports/{id}/review", reportId)
                .header("Authorization", managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new ReviewBody("REQUESTED_CHANGES", "Please add more detail"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NEEDS_CORRECTION"));
        flushAndClear();

        // Editing is allowed again now
        mockMvc.perform(put("/api/reports/{id}", reportId)
                .header("Authorization", memberToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        testDataFactory.reportRequest(project.getId(), LocalDate.now().minusWeeks(1)))))
                .andExpect(status().isOk());
        flushAndClear();
        addTask(reportId, memberToken, "Second task addressing feedback");

        submit(reportId, memberToken);

        List<ReportVersion> versionsAfterResubmit = reportVersionRepository
                .findByReportIdOrderByVersionNumberDesc(reportId);
        assertThat(versionsAfterResubmit).hasSize(2);
        ReportVersion version2 = versionsAfterResubmit.get(0);
        ReportVersion version1 = versionsAfterResubmit.get(1);
        assertThat(version2.getVersionNumber()).isEqualTo(2);
        assertThat(version1.getVersionNumber()).isEqualTo(1);
        assertThat(version1.getTasks()).hasSize(1); // unchanged - only "First task"
        assertThat(version2.getTasks()).hasSize(2); // "First task" + "Second task..."

        // Manager approves
        mockMvc.perform(post("/api/reports/{id}/review", reportId)
                .header("Authorization", managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ReviewBody("APPROVED", null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
        flushAndClear();

        // Further edit/submit/review are all rejected now
        mockMvc.perform(put("/api/reports/{id}", reportId)
                .header("Authorization", memberToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        testDataFactory.reportRequest(project.getId(), LocalDate.now().minusWeeks(1)))))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/reports/{id}/submit", reportId).header("Authorization", memberToken))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/reports/{id}/review", reportId)
                .header("Authorization", managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ReviewBody("APPROVED", null))))
                .andExpect(status().isConflict());

        // Review history: both actions, in order, referencing the correct version
        // numbers
        MvcResult historyResult = mockMvc.perform(get("/api/reports/{id}/review-history", reportId)
                .header("Authorization", memberToken))
                .andExpect(status().isOk())
                .andReturn();
        var historyJson = objectMapper.readTree(historyResult.getResponse().getContentAsString());
        assertThat(historyJson).hasSize(2);
        assertThat(historyJson.get(0).get("action").asText()).isEqualTo("REQUESTED_CHANGES");
        assertThat(historyJson.get(0).get("versionNumber").asInt()).isEqualTo(1);
        assertThat(historyJson.get(1).get("action").asText()).isEqualTo("APPROVED");
        assertThat(historyJson.get(1).get("versionNumber").asInt()).isEqualTo(2);
    }

    @Test
    void onlyOneBlockerCanBeTheKeyIssue() throws Exception {
        User member = testDataFactory.createTeamMember();
        Project project = testDataFactory.createProject("Workflow-KeyBlocker");
        String token = "Bearer " + testJwtFactory.tokenFor(member);
        Long reportId = createDraftReportViaHttp(member, project, token);

        MvcResult firstBlockerResult = mockMvc.perform(post("/api/reports/{id}/blockers", reportId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        testDataFactory.blockerRequest("First blocker", true))))
                .andExpect(status().isCreated())
                .andReturn();
        long firstBlockerId = objectMapper.readTree(firstBlockerResult.getResponse().getContentAsString())
                .get("id").asLong();
        flushAndClear();

        mockMvc.perform(post("/api/reports/{id}/blockers", reportId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        testDataFactory.blockerRequest("Second blocker", true))))
                .andExpect(status().isCreated());

        // Assert the actual persisted state, not just the response body of the second
        // call.
        ReportBlocker firstBlockerReloaded = reportBlockerRepository.findById(firstBlockerId).orElseThrow();
        assertThat(firstBlockerReloaded.isKeyIssue()).isFalse();

        long keyIssueCount = reportBlockerRepository.findByReportId(reportId).stream()
                .filter(ReportBlocker::isKeyIssue)
                .count();
        assertThat(keyIssueCount).isEqualTo(1);
    }

    private Long createDraftReportViaHttp(User member, Project project, String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/reports")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        testDataFactory.reportRequest(project.getId(), LocalDate.now().minusWeeks(1)))))
                .andExpect(status().isCreated())
                .andReturn();
        flushAndClear();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private void addTask(Long reportId, String token, String taskName) throws Exception {
        mockMvc.perform(post("/api/reports/{id}/tasks", reportId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testDataFactory.taskRequest(taskName))))
                .andExpect(status().isCreated());
        flushAndClear();
    }

    private void submit(Long reportId, String token) throws Exception {
        mockMvc.perform(post("/api/reports/{id}/submit", reportId).header("Authorization", token))
                .andExpect(status().isOk());
        flushAndClear();
    }

    private record ReviewBody(String action, String comment) {
    }
}
