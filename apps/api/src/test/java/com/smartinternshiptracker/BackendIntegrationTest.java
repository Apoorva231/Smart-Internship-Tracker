package com.smartinternshiptracker;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

class BackendIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void startsApplicationWithPostgresTestContainer() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void registersLogsInAndCreatesApplication() throws Exception {
        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Apoorva",
                                  "email": "apoorva@example.com",
                                  "password": "Password123!"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.email").value("apoorva@example.com"))
                .andReturn();

        String registerToken = objectMapper.readTree(registerResult.getResponse().getContentAsString())
                .path("token")
                .asString();
        assertFalse(registerToken.isBlank());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "apoorva@example.com",
                                  "password": "Password123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("apoorva@example.com"))
                .andReturn();

        String loginToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .path("token")
                .asString();
        assertFalse(loginToken.isBlank());

        mockMvc.perform(post("/api/applications")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "Software Engineering Intern",
                                  "companyName": "Shopify",
                                  "companyLocation": "Montreal, QC",
                                  "status": "APPLIED",
                                  "priority": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.application.role").value("Software Engineering Intern"))
                .andExpect(jsonPath("$.application.status").value("APPLIED"))
                .andExpect(jsonPath("$.application.priority").value(1))
                .andExpect(jsonPath("$.application.company.name").value("Shopify"));
    }

    @Test
    void returnsCurrentUserForBearerToken() throws Exception {
        String token = registerAndLogin("me@example.com");

        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.name").value("Apoorva"))
                .andExpect(jsonPath("$.user.email").value("me@example.com"))
                .andExpect(jsonPath("$.user.city").value("Montreal, QC"));
    }

    @Test
    void createsTaskForCreatedApplication() throws Exception {
        String token = registerAndLogin("task-flow@example.com");

        MvcResult applicationResult = mockMvc.perform(post("/api/applications")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "Backend Intern",
                                  "companyName": "Lightspeed",
                                  "status": "INTERVIEW",
                                  "priority": 2
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String applicationId = objectMapper.readTree(applicationResult.getResponse().getContentAsString())
                .path("application")
                .path("id")
                .asString();
        assertFalse(applicationId.isBlank());

        mockMvc.perform(post("/api/applications/{applicationId}/tasks", applicationId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Send follow-up email",
                                  "dueDate": "2026-09-01T09:00:00-04:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.task.title").value("Send follow-up email"))
                .andExpect(jsonPath("$.task.completed").value(false));
    }

    @Test
    void authenticatedUserOnlySeesOwnApplicationAndTaskData() throws Exception {
        String ownerToken = registerAndLogin("owner@example.com");
        String otherToken = registerAndLogin("other@example.com");

        MvcResult applicationResult = mockMvc.perform(post("/api/applications")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "Platform Intern",
                                  "companyName": "Dialogue",
                                  "status": "APPLIED",
                                  "priority": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String ownerApplicationId = objectMapper.readTree(applicationResult.getResponse().getContentAsString())
                .path("application")
                .path("id")
                .asString();
        assertFalse(ownerApplicationId.isBlank());

        mockMvc.perform(post("/api/applications/{applicationId}/tasks", ownerApplicationId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Prepare interview notes"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/applications")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applications").isEmpty());

        mockMvc.perform(post("/api/applications/{applicationId}/tasks", ownerApplicationId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Should not be allowed"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsInsightsForAuthenticatedUser() throws Exception {
        String token = registerAndLogin("insights@example.com");

        MvcResult applicationResult = mockMvc.perform(post("/api/applications")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "Data Intern",
                                  "companyName": "Coveo",
                                  "status": "INTERVIEW",
                                  "priority": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String applicationId = objectMapper.readTree(applicationResult.getResponse().getContentAsString())
                .path("application")
                .path("id")
                .asString();
        assertFalse(applicationId.isBlank());

        mockMvc.perform(post("/api/applications/{applicationId}/tasks", applicationId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Practice SQL questions",
                                  "dueDate": "2026-09-03T10:00:00-04:00"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/applications/insights")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metrics.total").value(1))
                .andExpect(jsonPath("$.metrics.active").value(1))
                .andExpect(jsonPath("$.metrics.interviews").value(1))
                .andExpect(jsonPath("$.metrics.offers").value(0))
                .andExpect(jsonPath("$.metrics.highPriority").value(1))
                .andExpect(jsonPath("$.counts[0].status").value("INTERVIEW"))
                .andExpect(jsonPath("$.counts[0]._count.status").value(1))
                .andExpect(jsonPath("$.upcomingTasks[0].title").value("Practice SQL questions"))
                .andExpect(jsonPath("$.upcomingTasks[0].application.role").value("Data Intern"));
    }

    @Test
    void searchesApplicationsWithAndWithoutStatusFilter() throws Exception {
        String token = registerAndLogin("search@example.com");

        mockMvc.perform(post("/api/applications")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "Data Engineer Intern",
                                  "companyName": "Pratt and Whitney",
                                  "companyLocation": "Montreal, QC",
                                  "status": "INTERVIEW",
                                  "priority": 2
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/applications")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "Frontend Intern",
                                  "companyName": "Lightspeed",
                                  "companyLocation": "Montreal, QC",
                                  "status": "SAVED",
                                  "priority": 2
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/applications")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .param("search", "pra"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applications.length()").value(1))
                .andExpect(jsonPath("$.applications[0].company.name").value("Pratt and Whitney"));

        mockMvc.perform(get("/api/applications")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .param("status", "INTERVIEW")
                        .param("search", "pra"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applications.length()").value(1))
                .andExpect(jsonPath("$.applications[0].status").value("INTERVIEW"));
    }

    @Test
    void companiesEndpointRequiresJwtAndWorksWithJwt() throws Exception {
        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized"));

        String token = registerAndLogin("companies@example.com");

        mockMvc.perform(post("/api/applications")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "Product Intern",
                                  "companyName": "Busbud",
                                  "companyLocation": "Montreal, QC",
                                  "status": "SAVED",
                                  "priority": 3
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/companies")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companies[0].name").value("Busbud"))
                .andExpect(jsonPath("$.companies[0].location").value("Montreal, QC"));
    }

    private String registerAndLogin(String email) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Apoorva",
                                  "email": "%s",
                                  "password": "Password123!"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "Password123!"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .path("token")
                .asString();
        assertFalse(token.isBlank());

        return token;
    }
}
