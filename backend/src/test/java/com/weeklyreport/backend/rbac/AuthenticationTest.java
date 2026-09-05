package com.weeklyreport.backend.rbac;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.weeklyreport.backend.entity.User;
import com.weeklyreport.backend.support.BaseIntegrationTest;
import com.weeklyreport.backend.support.TestDataFactory;

class AuthenticationTest extends BaseIntegrationTest {

    @Test
    void registeringAlwaysGetsTeamMemberRoleEvenIfClientTriesToSneakInAnotherRole() throws Exception {
        String rawBodyWithExtraRoleField = """
                {"name":"Casey Doe","email":"casey.doe@test.example.com","password":"Password123!","role":"ADMIN"}
                """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(rawBodyWithExtraRoleField))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("TEAM_MEMBER"));
    }

    @Test
    void protectedEndpointWithNoAuthorizationHeaderReturns401() throws Exception {
        mockMvc.perform(get("/api/reports/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointWithGarbageTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/reports/my")
                .header("Authorization", "Bearer not-a-real-jwt-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointWithExpiredTokenReturns401() throws Exception {
        User member = testDataFactory.createTeamMember();
        String expiredToken = testJwtFactory.expiredTokenFor(member);

        mockMvc.perform(get("/api/reports/my")
                .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void wrongPasswordAndNonexistentEmailReturnTheSameGenericMessage() throws Exception {
        User member = testDataFactory.createTeamMember();

        String wrongPasswordBody = objectMapper.writeValueAsString(
                new LoginAttempt(member.getEmail(), "totally-wrong-password"));
        String nonexistentEmailBody = objectMapper.writeValueAsString(
                new LoginAttempt("nobody-like-this-exists@test.example.com", "whatever123"));

        MvcResult wrongPasswordResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(wrongPasswordBody))
                .andExpect(status().isUnauthorized())
                .andReturn();

        MvcResult nonexistentEmailResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(nonexistentEmailBody))
                .andExpect(status().isUnauthorized())
                .andReturn();

        String wrongPasswordMessage = objectMapper.readTree(wrongPasswordResult.getResponse().getContentAsString())
                .get("message").asText();
        String nonexistentEmailMessage = objectMapper
                .readTree(nonexistentEmailResult.getResponse().getContentAsString())
                .get("message").asText();

        org.assertj.core.api.Assertions.assertThat(wrongPasswordMessage).isEqualTo(nonexistentEmailMessage);
    }

    private record LoginAttempt(String email, String password) {
    }
}
