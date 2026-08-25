package com.smartinternshiptracker.company;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CompanyController.class)
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompanyService companyService;

    @Test
    void listCompaniesReturnsCompaniesEnvelope() throws Exception {
        LocalDateTime now = LocalDateTime.parse("2026-08-24T12:00:00");

        when(companyService.listCompanies())
                .thenReturn(List.of(new CompanyResponse(
                        "company_123",
                        "Amazon",
                        "Montreal, QC",
                        "https://amazon.ca",
                        "Technology",
                        "10000+",
                        now,
                        now
                )));

        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companies").isArray())
                .andExpect(jsonPath("$.companies[0].id").value("company_123"))
                .andExpect(jsonPath("$.companies[0].name").value("Amazon"));
    }
}
