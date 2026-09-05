package com.weeklyreport.backend.rbac;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.weeklyreport.backend.entity.Project;
import com.weeklyreport.backend.entity.Report;
import com.weeklyreport.backend.entity.User;
import com.weeklyreport.backend.support.BaseIntegrationTest;

class AuthorizationTest extends BaseIntegrationTest {

    // --- Role separation ---

    @Test
    void teamMemberCannotReviewReports() throws Exception {
        User teamMember = testDataFactory.createTeamMember();
        Project project = testDataFactory.createProject("Role-Sep");
        Report report = testDataFactory.createDraftReport(teamMember, project, LocalDate.now().minusWeeks(1));

        String body = objectMapper.writeValueAsString(new ReviewBody("APPROVED", null));

        mockMvc.perform(post("/api/reports/{id}/review", report.getId())
                .header("Authorization", "Bearer " + testJwtFactory.tokenFor(teamMember))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void teamMemberCannotAccessAdminRoleEndpoint() throws Exception {
        User teamMember = testDataFactory.createTeamMember();
        User otherMember = testDataFactory.createTeamMember();

        mockMvc.perform(patch("/api/admin/users/{id}/role", otherMember.getId())
                .header("Authorization", "Bearer " + testJwtFactory.tokenFor(teamMember))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RoleBody("MANAGER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerCannotAccessAdminRoleEndpoint() throws Exception {
        User manager = testDataFactory.createManager();
        User teamMember = testDataFactory.createTeamMember();

        mockMvc.perform(patch("/api/admin/users/{id}/role", teamMember.getId())
                .header("Authorization", "Bearer " + testJwtFactory.tokenFor(manager))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RoleBody("ADMIN"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerCanAccessManagerEndpoints() throws Exception {
        User manager = testDataFactory.createManager();

        mockMvc.perform(get("/api/manager/dashboard/summary")
                .header("Authorization", "Bearer " + testJwtFactory.tokenFor(manager)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/manager/reports")
                .header("Authorization", "Bearer " + testJwtFactory.tokenFor(manager)))
                .andExpect(status().isOk());
    }

    @Test
    void adminCanAccessManagerEndpointsAndAdminOnlyEndpoints() throws Exception {
        User admin = testDataFactory.createAdmin();
        User teamMember = testDataFactory.createTeamMember();

        mockMvc.perform(get("/api/manager/dashboard/summary")
                .header("Authorization", "Bearer " + testJwtFactory.tokenFor(admin)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/manager/reports")
                .header("Authorization", "Bearer " + testJwtFactory.tokenFor(admin)))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/admin/users/{id}/role", teamMember.getId())
                .header("Authorization", "Bearer " + testJwtFactory.tokenFor(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RoleBody("MANAGER"))))
                .andExpect(status().isNoContent());
    }

    // --- Ownership ---

    @Test
    void differentTeamMemberCannotGetPutOrDeleteAnothersReport() throws Exception {
        User owner = testDataFactory.createTeamMember();
        User intruder = testDataFactory.createTeamMember();
        Project project = testDataFactory.createProject("Ownership");
        Report report = testDataFactory.createDraftReport(owner, project, LocalDate.now().minusWeeks(1));
        String intruderToken = "Bearer " + testJwtFactory.tokenFor(intruder);

        mockMvc.perform(get("/api/reports/{id}", report.getId()).header("Authorization", intruderToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/reports/{id}", report.getId())
                .header("Authorization", intruderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        testDataFactory.reportRequest(project.getId(), report.getWeekStart()))))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/reports/{id}", report.getId()).header("Authorization", intruderToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void differentTeamMemberCannotAddSubResourcesToAnothersReport() throws Exception {
        User owner = testDataFactory.createTeamMember();
        User intruder = testDataFactory.createTeamMember();
        Project project = testDataFactory.createProject("Ownership-Sub");
        Report report = testDataFactory.createDraftReport(owner, project, LocalDate.now().minusWeeks(1));
        String intruderToken = "Bearer " + testJwtFactory.tokenFor(intruder);

        mockMvc.perform(post("/api/reports/{id}/tasks", report.getId())
                .header("Authorization", intruderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testDataFactory.taskRequest("Sneaky task"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/reports/{id}/blockers", report.getId())
                .header("Authorization", intruderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testDataFactory.blockerRequest("Sneaky blocker", false))))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/reports/{id}/achievements", report.getId())
                .header("Authorization", intruderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        testDataFactory.achievementRequest("Sneaky achievement", false))))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/reports/{id}/hours", report.getId())
                .header("Authorization", intruderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        testDataFactory.hourRequest(
                                com.weeklyreport.backend.entity.enums.TaskType.DEVELOPMENT, 4.0))))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerCanReadButNotEditAnotherUsersReportContent() throws Exception {
        User owner = testDataFactory.createTeamMember();
        User manager = testDataFactory.createManager();
        Project project = testDataFactory.createProject("Manager-Read-Only");
        Report report = testDataFactory.createDraftReport(owner, project, LocalDate.now().minusWeeks(1));
        String managerToken = "Bearer " + testJwtFactory.tokenFor(manager);

        mockMvc.perform(get("/api/reports/{id}", report.getId()).header("Authorization", managerToken))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/reports/{id}", report.getId())
                .header("Authorization", managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        testDataFactory.reportRequest(project.getId(), report.getWeekStart()))))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/reports/{id}/tasks", report.getId())
                .header("Authorization", managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testDataFactory.taskRequest("Manager sneaky task"))))
                .andExpect(status().isForbidden());
    }

    private record ReviewBody(String action, String comment) {
    }

    private record RoleBody(String roleName) {
    }
}
