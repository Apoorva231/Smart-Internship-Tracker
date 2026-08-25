package com.smartinternshiptracker.company;

import java.time.LocalDateTime;

public record CompanyResponse(
        String id,
        String name,
        String location,
        String website,
        String industry,
        String size,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static CompanyResponse from(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getLocation(),
                company.getWebsite(),
                company.getIndustry(),
                company.getSize(),
                company.getCreatedAt(),
                company.getUpdatedAt()
        );
    }
}
