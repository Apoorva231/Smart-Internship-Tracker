package com.smartinternshiptracker.task;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, String> {

    List<Task> findByApplicationIdOrderByCompletedAscDueDateAscCreatedAtAsc(String applicationId);
    Optional<Task> findByIdAndApplicationUserId(String id, String userId);
    List<Task> findTop5ByCompletedFalseAndDueDateIsNotNullAndApplicationUserIdOrderByDueDateAsc(String userId);
}
