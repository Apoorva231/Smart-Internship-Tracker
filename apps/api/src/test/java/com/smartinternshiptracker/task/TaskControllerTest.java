package com.smartinternshiptracker.task;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
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
}
