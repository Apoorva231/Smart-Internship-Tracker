package com.smartinternshiptracker.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record TaskCreateRequest(
        @NotBlank
        @Size(min = 2, max = 140)
        String title,

        OffsetDateTime dueDate
) {
}
