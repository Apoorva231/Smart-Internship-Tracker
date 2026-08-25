package com.smartinternshiptracker.application;

import com.smartinternshiptracker.company.CompanyResponse;
import com.smartinternshiptracker.task.Task;
import java.time.LocalDateTime;
import java.util.List;

public record ApplicationInsightsResponse(
        List<StatusCountResponse> counts,
        MetricsResponse metrics,
        List<UpcomingTaskResponse> upcomingTasks
) {

    public record StatusCountResponse(
            ApplicationStatus status,
            CountResponse _count
    ) {
    }

    public record CountResponse(
            Long status
    ) {
    }

    public record MetricsResponse(
            int total,
            int active,
            int interviews,
            int offers,
            int highPriority
    ) {
    }

    public record UpcomingTaskResponse(
            String id,
            String title,
            LocalDateTime dueDate,
            Boolean completed,
            ApplicationSummaryResponse application
    ) {

        public static UpcomingTaskResponse from(Task task) {
            return new UpcomingTaskResponse(
                    task.getId(),
                    task.getTitle(),
                    task.getDueDate(),
                    task.getCompleted(),
                    ApplicationSummaryResponse.from(task.getApplication())
            );
        }
    }

    public record ApplicationSummaryResponse(
            String id,
            String role,
            ApplicationStatus status,
            CompanyResponse company
    ) {

        public static ApplicationSummaryResponse from(Application application) {
            return new ApplicationSummaryResponse(
                    application.getId(),
                    application.getRole(),
                    application.getStatus(),
                    CompanyResponse.from(application.getCompany())
            );
        }
    }
}