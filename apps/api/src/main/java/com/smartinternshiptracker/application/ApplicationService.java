package com.smartinternshiptracker.application;

import com.smartinternshiptracker.company.Company;
import com.smartinternshiptracker.company.CompanyNotFoundException;
import com.smartinternshiptracker.company.CompanyRepository;
import com.smartinternshiptracker.task.TaskRepository;
import com.smartinternshiptracker.user.User;
import com.smartinternshiptracker.user.UserRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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

    private static final Set<ApplicationStatus> INACTIVE_STATUSES = EnumSet.of(
            ApplicationStatus.REJECTED,
            ApplicationStatus.ARCHIVED
    );

    private static final Set<ApplicationStatus> INTERVIEW_STATUSES = EnumSet.of(
            ApplicationStatus.INTERVIEW,
            ApplicationStatus.TECHNICAL
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
        String normalizedSearch = hasText(search) ? search.trim() : null;

        List<Application> applications;
        if (normalizedSearch == null) {
            applications = status == null
                    ? applicationRepository.findByUserIdOrderByPriorityAscUpdatedAtDesc(userId)
                    : applicationRepository.findByUserIdAndStatusOrderByPriorityAscUpdatedAtDesc(userId, status);
        } else {
            applications = status == null
                    ? applicationRepository.searchApplications(userId, normalizedSearch)
                    : applicationRepository.searchApplicationsByStatus(userId, status, normalizedSearch);
        }

        return applications.stream()
                .map(this::toResponse)
                .toList();
    }

    public ApplicationInsightsResponse getInsights(String userId) {
        List<Application> applications = applicationRepository.findByUserIdOrderByPriorityAscUpdatedAtDesc(userId);
        Map<ApplicationStatus, Long> countsByStatus = applications.stream()
                .collect(Collectors.groupingBy(
                        Application::getStatus,
                        () -> new EnumMap<>(ApplicationStatus.class),
                        Collectors.counting()
                ));

        return new ApplicationInsightsResponse(
                Arrays.stream(ApplicationStatus.values())
                        .filter(countsByStatus::containsKey)
                        .map(status -> new ApplicationInsightsResponse.StatusCountResponse(
                                status,
                                new ApplicationInsightsResponse.CountResponse(countsByStatus.get(status))
                        ))
                        .toList(),
                new ApplicationInsightsResponse.MetricsResponse(
                        applications.size(),
                        (int) applications.stream()
                                .filter(application -> !INACTIVE_STATUSES.contains(application.getStatus()))
                                .count(),
                        (int) applications.stream()
                                .filter(application -> INTERVIEW_STATUSES.contains(application.getStatus()))
                                .count(),
                        (int) applications.stream()
                                .filter(application -> application.getStatus() == ApplicationStatus.OFFER)
                                .count(),
                        (int) applications.stream()
                                .filter(application -> application.getPriority() == 1)
                                .count()
                ),
                taskRepository.findTop5ByCompletedFalseAndDueDateIsNotNullAndApplicationUserIdOrderByDueDateAsc(userId)
                        .stream()
                        .map(ApplicationInsightsResponse.UpcomingTaskResponse::from)
                        .toList()
        );
    }

    public ApplicationResponse getApplication(String id, String userId) {
        Application application = applicationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(ApplicationNotFoundException::new);

        return toResponse(application);
    }

    public ApplicationResponse createApplication(String userId, ApplicationCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Company company = resolveCompany(
                request.companyId(),
                request.companyName(),
                request.companyLocation(),
                request.companyWebsite(),
                request.companyIndustry(),
                request.companySize()
        );
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

    public ApplicationResponse updateApplication(String id, String userId, ApplicationUpdateRequest request) {
        Application application = applicationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(ApplicationNotFoundException::new);

        ApplicationStatus nextStatus = request.status() == null ? application.getStatus() : request.status();
        boolean shouldSetAppliedAt = application.getAppliedAt() == null && SUBMITTED_STATUSES.contains(nextStatus);

        Company company = application.getCompany();
        if (hasText(request.companyId()) || hasText(request.companyName())) {
            company = resolveCompany(
                    request.companyId(),
                    request.companyName(),
                    request.companyLocation(),
                    request.companyWebsite(),
                    request.companyIndustry(),
                    request.companySize()
            );
        }

        application.updateDetails(
                request.role() == null ? application.getRole() : request.role().trim(),
                nextStatus,
                request.workMode() == null ? application.getWorkMode() : request.workMode(),
                request.priority() == null ? application.getPriority() : request.priority(),
                request.deadline() == null ? application.getDeadline() : request.deadline().toLocalDateTime(),
                request.jobUrl() == null ? application.getJobUrl() : request.jobUrl(),
                request.salaryRange() == null ? application.getSalaryRange() : request.salaryRange(),
                request.contactName() == null ? application.getContactName() : request.contactName(),
                request.contactEmail() == null ? application.getContactEmail() : request.contactEmail(),
                request.notes() == null ? application.getNotes() : request.notes(),
                shouldSetAppliedAt ? LocalDateTime.now() : application.getAppliedAt(),
                company
        );

        return toResponse(applicationRepository.save(application));
    }

    public void deleteApplication(String id, String userId) {
        Application application = applicationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(ApplicationNotFoundException::new);

        applicationRepository.delete(application);
    }

    private Company resolveCompany(
            String companyId,
            String companyName,
            String companyLocation,
            String companyWebsite,
            String companyIndustry,
            String companySize
    ) {
        if (hasText(companyId)) {
            return companyRepository.findById(companyId)
                    .orElseThrow(CompanyNotFoundException::new);
        }

        String name = companyName.trim();
        String location = companyLocationOrDefault(companyLocation);
        String industry = companyIndustryOrDefault(companyIndustry);

        Company company = companyRepository.findByNameAndLocation(name, location)
                .orElseGet(() -> new Company(
                        "company_" + java.util.UUID.randomUUID(),
                        name,
                        location,
                        companyWebsite,
                        industry,
                        companySize
                ));

        company.updateDetails(companyWebsite, industry, companySize);

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
