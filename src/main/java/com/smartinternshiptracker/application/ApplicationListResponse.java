package com.smartinternshiptracker.application;

import java.util.List;

public record ApplicationListResponse(
        List<ApplicationResponse> applications
) {
}