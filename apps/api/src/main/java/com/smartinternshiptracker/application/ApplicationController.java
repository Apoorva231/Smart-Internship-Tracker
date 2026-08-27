package com.smartinternshiptracker.application;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
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
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) @Size(max = 120) String search
    ) {
        return new ApplicationListResponse(
                applicationService.listApplications(jwt.getSubject(), status, search)
        );
    }

    @GetMapping("/insights")
    public ApplicationInsightsResponse getInsights(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return applicationService.getInsights(jwt.getSubject());
    }

    @GetMapping("/{id}")
    public ApplicationDetailResponse getApplication(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id
    ) {
        return new ApplicationDetailResponse(
                applicationService.getApplication(id, jwt.getSubject())
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationDetailResponse createApplication(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ApplicationCreateRequest request
    ) {
        return new ApplicationDetailResponse(
                applicationService.createApplication(jwt.getSubject(), request)
        );
    }

    @PatchMapping("/{id}")
    public ApplicationDetailResponse updateApplication(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id,
            @Valid @RequestBody ApplicationUpdateRequest request
    ) {
        return new ApplicationDetailResponse(
                applicationService.updateApplication(id, jwt.getSubject(), request)
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteApplication(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id
    ) {
        applicationService.deleteApplication(id, jwt.getSubject());
    }
}
