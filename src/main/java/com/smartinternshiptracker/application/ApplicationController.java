package com.smartinternshiptracker.application;

import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Validated
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public ApplicationListResponse listApplications(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) @Size(max = 120) String search
    ) {
        return new ApplicationListResponse(
                applicationService.listApplications(userId, status, search)
        );
    }

    @GetMapping("/{id}")
    public ApplicationDetailResponse getApplication(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id
    ) {
        return new ApplicationDetailResponse(
                applicationService.getApplication(id, userId)
        );
    }
}