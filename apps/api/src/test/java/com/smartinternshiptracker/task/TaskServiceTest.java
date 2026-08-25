package com.smartinternshiptracker.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.smartinternshiptracker.application.Application;
import com.smartinternshiptracker.application.ApplicationRepository;
import com.smartinternshiptracker.application.ApplicationStatus;
import com.smartinternshiptracker.application.WorkMode;
import com.smartinternshiptracker.company.Company;
import com.smartinternshiptracker.user.User;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void createTaskCreatesTaskForApplicationOwnedByUser() {
        User user = new User("user_123", "apoorva@example.com", "Apoorva", "hash", "Montreal, QC");
        Company company = new Company("company_123", "Amazon", "Montreal, QC", null, "Technology", null);
        Application application = new Application(
                "app_123",
                "Software Intern",
                ApplicationStatus.SAVED,
                WorkMode.HYBRID,
                2,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                user,
                company
        );

        when(applicationRepository.findByIdAndUserId("app_123", "user_123"))
                .thenReturn(Optional.of(application));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskCreateRequest request = new TaskCreateRequest(
                "Email recruiter",
                OffsetDateTime.parse("2026-08-28T12:00:00-04:00")
        );

        TaskResponse response = taskService.createTask("app_123", "user_123", request);

        assertEquals("Email recruiter", response.title());
        assertEquals("2026-08-28T12:00", response.dueDate().toString());
        assertFalse(response.completed());
    }

    @Test
    void updateTaskUpdatesTaskOwnedByUser() {
        User user = new User("user_123", "apoorva@example.com", "Apoorva", "hash", "Montreal, QC");
        Company company = new Company("company_123", "Amazon", "Montreal, QC", null, "Technology", null);
        Application application = new Application(
                "app_123",
                "Software Intern",
                ApplicationStatus.SAVED,
                WorkMode.HYBRID,
                2,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                user,
                company
        );
        Task task = new Task(
                "task_123",
                "Email recruiter",
                OffsetDateTime.parse("2026-08-28T12:00:00-04:00").toLocalDateTime(),
                false,
                application
        );

        when(taskRepository.findByIdAndApplicationUserId("task_123", "user_123"))
                .thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskUpdateRequest request = new TaskUpdateRequest(
                "Send follow-up email",
                OffsetDateTime.parse("2026-08-29T09:00:00-04:00"),
                true
        );

        TaskResponse response = taskService.updateTask("task_123", "user_123", request);

        assertEquals("Send follow-up email", response.title());
        assertEquals("2026-08-29T09:00", response.dueDate().toString());
        assertEquals(true, response.completed());
    }

    @Test
    void deleteTaskDeletesTaskOwnedByUser() {
        User user = new User("user_123", "apoorva@example.com", "Apoorva", "hash", "Montreal, QC");
        Company company = new Company("company_123", "Amazon", "Montreal, QC", null, "Technology", null);
        Application application = new Application(
                "app_123",
                "Software Intern",
                ApplicationStatus.SAVED,
                WorkMode.HYBRID,
                2,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                user,
                company
        );
        Task task = new Task(
                "task_123",
                "Email recruiter",
                null,
                false,
                application
        );

        when(taskRepository.findByIdAndApplicationUserId("task_123", "user_123"))
                .thenReturn(Optional.of(task));

        taskService.deleteTask("task_123", "user_123");

        verify(taskRepository).delete(task);
    }

}
