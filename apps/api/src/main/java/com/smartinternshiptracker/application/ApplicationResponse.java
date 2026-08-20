package com.smartinternshiptracker.application;

import com.smartinternshiptracker.company.Company;
import com.smartinternshiptracker.task.Task;
import java.time.LocalDateTime;
import java.util.List;

public record ApplicationResponse(
        String id,
        String role,
        ApplicationStatus status,
        WorkMode workMode,
        Integer priority,
        LocalDateTime deadline,
        String jobUrl,
        String salaryRange,
        String contactName,
        String contactEmail,
        String notes,
        LocalDateTime appliedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        CompanyResponse company,
        List<TaskResponse> tasks
) {

    public static ApplicationResponse from(Application application, List<Task> tasks) {
        return new ApplicationResponse(
                application.getId(),
                application.getRole(),
                application.getStatus(),
                application.getWorkMode(),
                application.getPriority(),
                application.getDeadline(),
                application.getJobUrl(),
                application.getSalaryRange(),
                application.getContactName(),
                application.getContactEmail(),
                application.getNotes(),
                application.getAppliedAt(),
                application.getCreatedAt(),
                application.getUpdatedAt(),
                CompanyResponse.from(application.getCompany()),
                tasks.stream().map(TaskResponse::from).toList()
        );
    }

    public record CompanyResponse(
            String id,
            String name,
            String location,
            String website,
            String industry,
            String size,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {

        public static CompanyResponse from(Company company) {
            return new CompanyResponse(
                    company.getId(),
                    company.getName(),
                    company.getLocation(),
                    company.getWebsite(),
                    company.getIndustry(),
                    company.getSize(),
                    company.getCreatedAt(),
                    company.getUpdatedAt()
            );
        }
    }

    public record TaskResponse(
            String id,
            String title,
            LocalDateTime dueDate,
            Boolean completed,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {

        public static TaskResponse from(Task task) {
            return new TaskResponse(
                    task.getId(),
                    task.getTitle(),
                    task.getDueDate(),
                    task.getCompleted(),
                    task.getCreatedAt(),
                    task.getUpdatedAt()
            );
        }
    }
}