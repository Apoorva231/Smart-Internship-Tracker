package com.smartinternshiptracker.application;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import org.hibernate.validator.constraints.URL;
import java.time.OffsetDateTime;

public record ApplicationCreateRequest(
        @NotBlank
        @Size(min = 2, max = 120)
        String role,

        String companyId,

        @Size(min = 2, max = 120)
        String companyName,

        @Size(min = 2, max = 120)
        String companyLocation,

        @URL
        String companyWebsite,

        @Size(min = 2, max = 80)
        String companyIndustry,

        @Size(max = 40)
        String companySize,

        ApplicationStatus status,

        WorkMode workMode,

        @Min(1)
        @Max(3)
        Integer priority,

        OffsetDateTime deadline,

        @URL
        String jobUrl,

        @Size(max = 80)
        String salaryRange,

        @Size(max = 80)
        String contactName,

        @Email
        String contactEmail,

        @Size(max = 2000)
        String notes
) {

    @AssertTrue(message = "Choose an existing company or enter a new company name")
    public boolean isCompanyReferencePresent() {
        return hasText(companyId) || hasText(companyName);
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}