package com.smartinternshiptracker.task;

import com.smartinternshiptracker.application.Application;
import com.smartinternshiptracker.application.ApplicationNotFoundException;
import com.smartinternshiptracker.application.ApplicationRepository;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ApplicationRepository applicationRepository;

    public TaskService(TaskRepository taskRepository, ApplicationRepository applicationRepository) {
        this.taskRepository = taskRepository;
        this.applicationRepository = applicationRepository;
    }

    public TaskResponse createTask(String applicationId, String userId, TaskCreateRequest request) {
        Application application = applicationRepository.findByIdAndUserId(applicationId, userId)
                .orElseThrow(ApplicationNotFoundException::new);

        Task task = new Task(
                "task_" + java.util.UUID.randomUUID(),
                request.title().trim(),
                request.dueDate() == null ? null : request.dueDate().toLocalDateTime(),
                false,
                application
        );

        return TaskResponse.from(taskRepository.save(task));
    }
}
