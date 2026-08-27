package com.smartinternshiptracker.company;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.smartinternshiptracker.auth.JwtProperties;
import com.smartinternshiptracker.auth.JwtService;
import com.smartinternshiptracker.auth.SecurityConfig;
import com.smartinternshiptracker.user.User;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CompanyController.class)
@Import(SecurityConfig.class)
@ImportAutoConfiguration({
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        ServletWebSecurityAutoConfiguration.class
})
@TestPropertySource(properties = {
        "app.jwt.secret=01234567890123456789012345678901",
        "app.jwt.expiration-minutes=60",
        "app.cors.allowed-origins=http://localhost:5173"
})
class CompanyControllerTest {

    private static final String JWT_SECRET = "01234567890123456789012345678901";

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

        mockMvc.perform(get("/api/companies")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companies").isArray())
                .andExpect(jsonPath("$.companies[0].id").value("company_123"))
                .andExpect(jsonPath("$.companies[0].name").value("Amazon"));
    }

    @Test
    void listCompaniesRequiresBearerToken() throws Exception {
        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    private String bearerToken() {
        JwtService jwtService = new JwtService(new JwtProperties(JWT_SECRET, 60));
        User user = new User(
                "user_123",
                "apoorva@example.com",
                "Apoorva",
                "hashed_password",
                "Montreal, QC"
        );

        return "Bearer " + jwtService.generateToken(user);
    }
}
