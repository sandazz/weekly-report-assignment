package com.weeklyreport.backend.filtering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.weeklyreport.backend.entity.Project;
import com.weeklyreport.backend.entity.User;
import com.weeklyreport.backend.entity.enums.ReportStatus;
import com.weeklyreport.backend.support.BaseIntegrationTest;

// Own fixtures only, deliberately not dependent on Phase 11's seed data - deterministic and
// independent of whatever else may or may not have been seeded.
class ReportFilteringTest extends BaseIntegrationTest {

    @Test
    void statusFilterOnlyReturnsThatUsersReportsWithThatStatus() throws Exception {
        User member = testDataFactory.createTeamMember();
        Project project = testDataFactory.createProject("Filter-Status");
        String token = "Bearer " + testJwtFactory.tokenFor(member);

        testDataFactory.createReportWithStatus(member, project, LocalDate.now().minusWeeks(1), ReportStatus.DRAFT);
        testDataFactory.createReportWithStatus(member, project, LocalDate.now().minusWeeks(2), ReportStatus.DRAFT);
        testDataFactory.createReportWithStatus(member, project, LocalDate.now().minusWeeks(3), ReportStatus.SUBMITTED);

        mockMvc.perform(get("/api/reports/my")
                .header("Authorization", token)
                .param("status", "DRAFT"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    var json = objectMapper.readTree(result.getResponse().getContentAsString());
                    assertThat(json.get("totalElements").asInt()).isEqualTo(2);
                    json.get("content").forEach(report -> assertThat(report.get("status").asText()).isEqualTo("DRAFT"));
                });
    }

    @Test
    void projectFilterOnManagerEndpointReturnsOnlyThatProjectsReportsRegardlessOfOwner() throws Exception {
        User memberA = testDataFactory.createTeamMember();
        User memberB = testDataFactory.createTeamMember();
        User manager = testDataFactory.createManager();
        Project projectA = testDataFactory.createProject("Filter-ProjectA");
        Project projectB = testDataFactory.createProject("Filter-ProjectB");
        String managerToken = "Bearer " + testJwtFactory.tokenFor(manager);

        testDataFactory.createReportWithStatus(memberA, projectA, LocalDate.now().minusWeeks(1),
                ReportStatus.SUBMITTED);
        testDataFactory.createReportWithStatus(memberB, projectA, LocalDate.now().minusWeeks(2),
                ReportStatus.SUBMITTED);
        testDataFactory.createReportWithStatus(memberA, projectB, LocalDate.now().minusWeeks(3),
                ReportStatus.SUBMITTED);

        mockMvc.perform(get("/api/manager/reports")
                .header("Authorization", managerToken)
                .param("projectId", String.valueOf(projectA.getId())))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    var json = objectMapper.readTree(result.getResponse().getContentAsString());
                    assertThat(json.get("totalElements").asInt()).isEqualTo(2);
                    json.get("content").forEach(
                            report -> assertThat(report.get("projectName").asText()).isEqualTo(projectA.getName()));
                });
    }

    @Test
    void paginationReturnsCorrectSecondPageAndTotals() throws Exception {
        User member = testDataFactory.createTeamMember();
        Project project = testDataFactory.createProject("Filter-Pagination");
        String token = "Bearer " + testJwtFactory.tokenFor(member);

        for (int i = 1; i <= 15; i++) {
            testDataFactory.createReportWithStatus(
                    member, project, LocalDate.now().minusWeeks(i), ReportStatus.DRAFT);
        }

        mockMvc.perform(get("/api/reports/my")
                .header("Authorization", token)
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    var json = objectMapper.readTree(result.getResponse().getContentAsString());
                    assertThat(json.get("totalElements").asInt()).isEqualTo(15);
                    assertThat(json.get("totalPages").asInt()).isEqualTo(2);
                    assertThat(json.get("number").asInt()).isEqualTo(1);
                    assertThat(json.get("content")).hasSize(5);
                });
    }

    @Test
    void dateRangeFilterOnlyReturnsReportsWithWeekStartInRange() throws Exception {
        User member = testDataFactory.createTeamMember();
        Project project = testDataFactory.createProject("Filter-DateRange");
        String token = "Bearer " + testJwtFactory.tokenFor(member);

        LocalDate inRangeWeek1 = LocalDate.now().minusWeeks(2);
        LocalDate inRangeWeek2 = LocalDate.now().minusWeeks(3);
        LocalDate outOfRangeWeek = LocalDate.now().minusWeeks(10);

        testDataFactory.createReportWithStatus(member, project, inRangeWeek1, ReportStatus.DRAFT);
        testDataFactory.createReportWithStatus(member, project, inRangeWeek2, ReportStatus.DRAFT);
        testDataFactory.createReportWithStatus(member, project, outOfRangeWeek, ReportStatus.DRAFT);

        mockMvc.perform(get("/api/reports/my")
                .header("Authorization", token)
                .param("fromDate", inRangeWeek2.toString())
                .param("toDate", inRangeWeek1.toString()))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    var json = objectMapper.readTree(result.getResponse().getContentAsString());
                    assertThat(json.get("totalElements").asInt()).isEqualTo(2);
                });
    }
}
