package com.smartinternshiptracker.task;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/api/applications/{applicationId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDetailResponse createTask(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String applicationId,
            @Valid @RequestBody TaskCreateRequest request
    ) {
        return new TaskDetailResponse(
                taskService.createTask(applicationId, userId, request)
        );
    }

    @PatchMapping("/api/tasks/{taskId}")
    public TaskDetailResponse updateTask(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String taskId,
            @Valid @RequestBody TaskUpdateRequest request
    ) {
        return new TaskDetailResponse(
                taskService.updateTask(taskId, userId, request)
        );
    }

    @DeleteMapping("/api/tasks/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String taskId
    ) {
        taskService.deleteTask(taskId, userId);
    }

}
