package com.smartinternshiptracker.task;

import java.time.LocalDateTime;

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
