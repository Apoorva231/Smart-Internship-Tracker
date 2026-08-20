package com.smartinternshiptracker.application;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationDetailResponse createApplication(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ApplicationCreateRequest request
    ) {
        return new ApplicationDetailResponse(
                applicationService.createApplication(userId, request)
        );
    }

    @PatchMapping("/{id}")
    public ApplicationDetailResponse updateApplication(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id,
            @Valid @RequestBody ApplicationUpdateRequest request
    ) {
        return new ApplicationDetailResponse(
                applicationService.updateApplication(id, userId, request)
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteApplication(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id
    ) {
        applicationService.deleteApplication(id, userId);
    }
}
