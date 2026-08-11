package com.smartinternshiptracker.task;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, String> {

    List<Task> findByApplicationIdOrderByCompletedAscDueDateAscCreatedAtAsc(String applicationId);
}
