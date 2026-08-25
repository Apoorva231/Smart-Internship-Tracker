package com.smartinternshiptracker.company;

import java.util.List;

public record CompanyListResponse(
        List<CompanyResponse> companies
) {
}
