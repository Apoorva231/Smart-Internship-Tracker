package com.smartinternshiptracker.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ApplicationController.class)
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApplicationService applicationService;

    @Test
    void listApplicationsReturnsApplicationsEnvelope() throws Exception {
        when(applicationService.listApplications("user_123", ApplicationStatus.APPLIED, "amazon"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/applications")
                        .header("X-User-Id", "user_123")
                        .param("status", "APPLIED")
                        .param("search", "amazon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applications").isArray());

        verify(applicationService).listApplications("user_123", ApplicationStatus.APPLIED, "amazon");
    }

    @Test
    void getApplicationReturnsApplicationEnvelope() throws Exception {
        LocalDateTime now = LocalDateTime.parse("2026-08-17T12:00:00");

        ApplicationResponse response = new ApplicationResponse(
                "app_123",
                "Software Intern",
                ApplicationStatus.APPLIED,
                WorkMode.HYBRID,
                1,
                null,
                null,
                null,
                null,
                null,
                null,
                now,
                now,
                now,
                new ApplicationResponse.CompanyResponse(
                        "company_123",
                        "Amazon",
                        "Montreal, QC",
                        null,
                        "Technology",
                        null,
                        now,
                        now
                ),
                List.of()
        );

        when(applicationService.getApplication("app_123", "user_123"))
                .thenReturn(response);

        mockMvc.perform(get("/api/applications/app_123")
                        .header("X-User-Id", "user_123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application.id").value("app_123"))
                .andExpect(jsonPath("$.application.role").value("Software Intern"))
                .andExpect(jsonPath("$.application.company.name").value("Amazon"))
                .andExpect(jsonPath("$.application.tasks").isArray());

        verify(applicationService).getApplication("app_123", "user_123");
    }

    @Test
    void applicationNotFoundReturnsNotFoundMessage() throws Exception {
        doThrow(new ApplicationNotFoundException())
                .when(applicationService)
                .getApplication("app_missing", "user_123");

        mockMvc.perform(get("/api/applications/app_missing")
                        .header("X-User-Id", "user_123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Application not found"));
    }

    @Test
    void createApplicationReturnsCreatedApplicationEnvelope() throws Exception {
        LocalDateTime now = LocalDateTime.parse("2026-08-18T13:00:00");

        ApplicationResponse response = new ApplicationResponse(
                "app_123",
                "Software Intern",
                ApplicationStatus.APPLIED,
                WorkMode.HYBRID,
                1,
                null,
                null,
                null,
                null,
                null,
                null,
                now,
                now,
                now,
                new ApplicationResponse.CompanyResponse(
                        "company_123",
                        "Amazon",
                        "Montreal, QC",
                        null,
                        "Technology",
                        null,
                        now,
                        now
                ),
                List.of()
        );

        when(applicationService.createApplication(eq("user_123"), any(ApplicationCreateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/applications")
                        .header("X-User-Id", "user_123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "Software Intern",
                                  "companyName": "Amazon",
                                  "status": "APPLIED",
                                  "priority": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.application.id").value("app_123"))
                .andExpect(jsonPath("$.application.role").value("Software Intern"))
                .andExpect(jsonPath("$.application.status").value("APPLIED"))
                .andExpect(jsonPath("$.application.company.name").value("Amazon"));

        verify(applicationService).createApplication(eq("user_123"), any(ApplicationCreateRequest.class));
    }

    @Test
    void createApplicationRequiresCompanyReference() throws Exception {
        mockMvc.perform(post("/api/applications")
                        .header("X-User-Id", "user_123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "Software Intern"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateApplicationReturnsApplicationEnvelope() throws Exception {
        LocalDateTime now = LocalDateTime.parse("2026-08-20T08:00:00");

        ApplicationResponse response = new ApplicationResponse(
                "app_123",
                "Backend Intern",
                ApplicationStatus.INTERVIEW,
                WorkMode.REMOTE,
                1,
                null,
                null,
                null,
                null,
                null,
                "Updated notes",
                now,
                now,
                now,
                new ApplicationResponse.CompanyResponse(
                        "company_123",
                        "Amazon",
                        "Montreal, QC",
                        null,
                        "Technology",
                        null,
                        now,
                        now
                ),
                List.of()
        );

        when(applicationService.updateApplication(eq("app_123"), eq("user_123"), any(ApplicationUpdateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/applications/app_123")
                        .header("X-User-Id", "user_123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "Backend Intern",
                                  "status": "INTERVIEW",
                                  "workMode": "REMOTE",
                                  "notes": "Updated notes"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application.id").value("app_123"))
                .andExpect(jsonPath("$.application.role").value("Backend Intern"))
                .andExpect(jsonPath("$.application.status").value("INTERVIEW"))
                .andExpect(jsonPath("$.application.workMode").value("REMOTE"))
                .andExpect(jsonPath("$.application.notes").value("Updated notes"));

        verify(applicationService).updateApplication(eq("app_123"), eq("user_123"), any(ApplicationUpdateRequest.class));
    }
}
