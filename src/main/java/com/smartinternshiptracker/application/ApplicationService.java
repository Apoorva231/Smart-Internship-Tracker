package com.smartinternshiptracker.application;

import com.smartinternshiptracker.company.Company;
import com.smartinternshiptracker.company.CompanyRepository;
import com.smartinternshiptracker.task.TaskRepository;
import com.smartinternshiptracker.user.UserRepository;
import com.smartinternshiptracker.user.User;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final CompanyRepository companyRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    private static final Set<ApplicationStatus> SUBMITTED_STATUSES = EnumSet.of(
        ApplicationStatus.APPLIED,
        ApplicationStatus.INTERVIEW,
        ApplicationStatus.TECHNICAL,
        ApplicationStatus.OFFER
    );

    public ApplicationService(
            ApplicationRepository applicationRepository,
            CompanyRepository companyRepository,
            TaskRepository taskRepository,
            UserRepository userRepository
    ) {
        this.applicationRepository = applicationRepository;
        this.companyRepository = companyRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public List<ApplicationResponse> listApplications(String userId, ApplicationStatus status, String search) {
        List<Application> applications = applicationRepository.searchApplications(
                userId,
                status,
                hasText(search) ? search.trim() : null
        );

        return applications.stream()
                .map(this::toResponse)
                .toList();
    }

    public ApplicationResponse getApplication(String id, String userId) {
        Application application = applicationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(ApplicationNotFoundException::new);

        return toResponse(application);
    }

    public ApplicationResponse createApplication(String userId, ApplicationCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Company company = resolveCompany(request);
        ApplicationStatus status = statusOrDefault(request.status());

        Application application = new Application(
                "app_" + java.util.UUID.randomUUID(),
                request.role().trim(),
                status,
                workModeOrDefault(request.workMode()),
                priorityOrDefault(request.priority()),
                request.deadline() == null ? null : request.deadline().toLocalDateTime(),
                request.jobUrl(),
                request.salaryRange(),
                request.contactName(),
                request.contactEmail(),
                request.notes(),
                SUBMITTED_STATUSES.contains(status) ? LocalDateTime.now() : null,
                user,
                company
        );

        return toResponse(applicationRepository.save(application));
    }

    private Company resolveCompany(ApplicationCreateRequest request) {
        if (hasText(request.companyId())) {
            return companyRepository.findById(request.companyId())
                    .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        }

        String name = request.companyName().trim();
        String location = companyLocationOrDefault(request.companyLocation());
        String industry = companyIndustryOrDefault(request.companyIndustry());

        Company company = companyRepository.findByNameAndLocation(name, location)
                .orElseGet(() -> new Company(
                        "company_" + java.util.UUID.randomUUID(),
                        name,
                        location,
                        request.companyWebsite(),
                        industry,
                        request.companySize()
                ));

        company.updateDetails(request.companyWebsite(), industry, request.companySize());

        return companyRepository.save(company);
    }

    private ApplicationResponse toResponse(Application application) {
        return ApplicationResponse.from(
                application,
                taskRepository.findByApplicationIdOrderByCompletedAscDueDateAscCreatedAtAsc(application.getId())
        );
    }

    private ApplicationStatus statusOrDefault(ApplicationStatus status) {
        return status == null ? ApplicationStatus.SAVED : status;
    }

    private WorkMode workModeOrDefault(WorkMode workMode) {
        return workMode == null ? WorkMode.HYBRID : workMode;
    }

    private Integer priorityOrDefault(Integer priority) {
        return priority == null ? 2 : priority;
    }

    private String companyLocationOrDefault(String companyLocation) {
        return hasText(companyLocation) ? companyLocation.trim() : "Montreal, QC";
    }

    private String companyIndustryOrDefault(String companyIndustry) {
        return hasText(companyIndustry) ? companyIndustry.trim() : "Technology";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

}
