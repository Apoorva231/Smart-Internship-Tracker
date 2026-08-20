package com.smartinternshiptracker.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.smartinternshiptracker.company.Company;
import com.smartinternshiptracker.company.CompanyRepository;
import com.smartinternshiptracker.task.TaskRepository;
import com.smartinternshiptracker.user.User;
import com.smartinternshiptracker.user.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ApplicationService applicationService;

    @Test
    void createApplicationCreatesNewCompanyAndAppliesDefaults() {
        User user = new User("user_123", "apoorva@example.com", "Apoorva", "hash", "Montreal, QC");
        Company savedCompany = new Company("company_123", "Amazon", "Montreal, QC", null, "Technology", null);

        when(userRepository.findById("user_123")).thenReturn(Optional.of(user));
        when(companyRepository.findByNameAndLocation("Amazon", "Montreal, QC")).thenReturn(Optional.empty());
        when(companyRepository.save(any(Company.class))).thenReturn(savedCompany);
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskRepository.findByApplicationIdOrderByCompletedAscDueDateAscCreatedAtAsc(any(String.class)))
                .thenReturn(List.of());

        ApplicationCreateRequest request = new ApplicationCreateRequest(
                "Software Intern",
                null,
                "Amazon",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        ApplicationResponse response = applicationService.createApplication("user_123", request);

        assertEquals("Software Intern", response.role());
        assertEquals(ApplicationStatus.SAVED, response.status());
        assertEquals(WorkMode.HYBRID, response.workMode());
        assertEquals(2, response.priority());
        assertNull(response.appliedAt());
        assertEquals("Amazon", response.company().name());
    }

    @Test
    void createApplicationSetsAppliedAtForSubmittedStatus() {
        User user = new User("user_123", "apoorva@example.com", "Apoorva", "hash", "Montreal, QC");
        Company savedCompany = new Company("company_123", "Amazon", "Montreal, QC", null, "Technology", null);

        when(userRepository.findById("user_123")).thenReturn(Optional.of(user));
        when(companyRepository.findByNameAndLocation("Amazon", "Montreal, QC")).thenReturn(Optional.empty());
        when(companyRepository.save(any(Company.class))).thenReturn(savedCompany);
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskRepository.findByApplicationIdOrderByCompletedAscDueDateAscCreatedAtAsc(any(String.class)))
                .thenReturn(List.of());

        ApplicationCreateRequest request = new ApplicationCreateRequest(
                "Software Intern",
                null,
                "Amazon",
                null,
                null,
                null,
                null,
                ApplicationStatus.APPLIED,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        ApplicationResponse response = applicationService.createApplication("user_123", request);

        assertEquals(ApplicationStatus.APPLIED, response.status());
        assertNotNull(response.appliedAt());
    }

    @Test
    void updateApplicationSetsAppliedAtWhenMovingToSubmittedStatus() {
        User user = new User(
                "user_123",
                "apoorva@example.com",
                "Apoorva",
                "password_hash",
                "Montreal, QC"
        );

        Company company = new Company(
                "company_123",
                "Amazon",
                "Montreal, QC",
                null,
                "Technology",
                null
        );

        Application application = new Application(
                "app_123",
                "Software Intern",
                ApplicationStatus.SAVED,
                WorkMode.HYBRID,
                2,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                user,
                company
        );

        ApplicationUpdateRequest request = new ApplicationUpdateRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                ApplicationStatus.APPLIED,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(applicationRepository.findByIdAndUserId("app_123", "user_123"))
                .thenReturn(Optional.of(application));
        when(applicationRepository.save(any(Application.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(taskRepository.findByApplicationIdOrderByCompletedAscDueDateAscCreatedAtAsc("app_123"))
                .thenReturn(List.of());

        ApplicationResponse response = applicationService.updateApplication("app_123", "user_123", request);

        assertEquals(ApplicationStatus.APPLIED, response.status());
        assertNotNull(response.appliedAt());
    }

    @Test
    void deleteApplicationDeletesExistingApplication() {
        User user = new User("user_123", "apoorva@example.com", "Apoorva", "hash", "Montreal, QC");
        Company company = new Company("company_123", "Amazon", "Montreal, QC", null, "Technology", null);

        Application application = new Application(
                "app_123",
                "Software Intern",
                ApplicationStatus.SAVED,
                WorkMode.HYBRID,
                2,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                user,
                company
        );

        when(applicationRepository.findByIdAndUserId("app_123", "user_123"))
                .thenReturn(Optional.of(application));

        applicationService.deleteApplication("app_123", "user_123");

        verify(applicationRepository).delete(application);
    }

}
