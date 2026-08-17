package com.smartinternshiptracker.application;

import com.smartinternshiptracker.company.CompanyRepository;
import com.smartinternshiptracker.task.TaskRepository;
import com.smartinternshiptracker.user.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final CompanyRepository companyRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

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

    private ApplicationResponse toResponse(Application application) {
        return ApplicationResponse.from(
                application,
                taskRepository.findByApplicationIdOrderByCompletedAscDueDateAscCreatedAtAsc(application.getId())
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

}