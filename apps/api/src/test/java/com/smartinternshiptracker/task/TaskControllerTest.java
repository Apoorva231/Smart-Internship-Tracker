package com.smartinternshiptracker.task;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Test
    void createTaskReturnsCreatedTaskEnvelope() throws Exception {
        LocalDateTime now = LocalDateTime.parse("2026-08-25T10:00:00");

        when(taskService.createTask(eq("app_123"), eq("user_123"), any(TaskCreateRequest.class)))
                .thenReturn(new TaskResponse(
                        "task_123",
                        "Email recruiter",
                        LocalDateTime.parse("2026-08-28T12:00:00"),
                        false,
                        now,
                        now
                ));

        mockMvc.perform(post("/api/applications/app_123/tasks")
                        .header("X-User-Id", "user_123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Email recruiter",
                                  "dueDate": "2026-08-28T12:00:00-04:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.task.id").value("task_123"))
                .andExpect(jsonPath("$.task.title").value("Email recruiter"))
                .andExpect(jsonPath("$.task.completed").value(false));
    }

    @Test
    void updateTaskReturnsUpdatedTaskEnvelope() throws Exception {
        LocalDateTime now = LocalDateTime.parse("2026-08-25T10:00:00");

        when(taskService.updateTask(eq("task_123"), eq("user_123"), any(TaskUpdateRequest.class)))
                .thenReturn(new TaskResponse(
                        "task_123",
                        "Send follow-up email",
                        LocalDateTime.parse("2026-08-29T09:00:00"),
                        true,
                        now,
                        now
                ));

        mockMvc.perform(patch("/api/tasks/task_123")
                        .header("X-User-Id", "user_123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Send follow-up email",
                                  "dueDate": "2026-08-29T09:00:00-04:00",
                                  "completed": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.task.id").value("task_123"))
                .andExpect(jsonPath("$.task.title").value("Send follow-up email"))
                .andExpect(jsonPath("$.task.completed").value(true));
    }

    @Test
    void deleteTaskReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/tasks/task_123")
                        .header("X-User-Id", "user_123"))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask("task_123", "user_123");
    }

}
