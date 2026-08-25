package com.smartinternshiptracker.task;

import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record TaskUpdateRequest(
        @Size(min = 2, max = 140)
        String title,

        OffsetDateTime dueDate,

        Boolean completed
) {
}
