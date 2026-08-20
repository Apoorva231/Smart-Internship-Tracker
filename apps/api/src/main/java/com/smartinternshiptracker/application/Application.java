package com.smartinternshiptracker.application;

import com.smartinternshiptracker.company.Company;
import com.smartinternshiptracker.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "applications")
public class Application {

    @Id
    private String id;

    @Column(nullable = false)
    private String role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.SAVED;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_mode", nullable = false)
    private WorkMode workMode = WorkMode.HYBRID;

    @Column(nullable = false)
    private Integer priority = 2;

    private LocalDateTime deadline;

    @Column(name = "job_url")
    private String jobUrl;

    @Column(name = "salary_range")
    private String salaryRange;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "contact_email")
    private String contactEmail;

    private String notes;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id")
    private Company company;

    protected Application() {
    }

    public Application(String id, String role, ApplicationStatus status, WorkMode workMode, Integer priority,
            LocalDateTime deadline, String jobUrl, String salaryRange, String contactName, String contactEmail,
            String notes, LocalDateTime appliedAt, User user, Company company) {
        this.id = id;
        this.role = role;
        this.status = status;
        this.workMode = workMode;
        this.priority = priority;
        this.deadline = deadline;
        this.jobUrl = jobUrl;
        this.salaryRange = salaryRange;
        this.contactName = contactName;
        this.contactEmail = contactEmail;
        this.notes = notes;
        this.appliedAt = appliedAt;
        this.user = user;
        this.company = company;
    }

    public void updateDetails(
            String role,
            ApplicationStatus status,
            WorkMode workMode,
            Integer priority,
            LocalDateTime deadline,
            String jobUrl,
            String salaryRange,
            String contactName,
            String contactEmail,
            String notes,
            LocalDateTime appliedAt,
            Company company
    ) {
        this.role = role;
        this.status = status;
        this.workMode = workMode;
        this.priority = priority;
        this.deadline = deadline;
        this.jobUrl = jobUrl;
        this.salaryRange = salaryRange;
        this.contactName = contactName;
        this.contactEmail = contactEmail;
        this.notes = notes;
        this.appliedAt = appliedAt;
        this.company = company;
    }

    public String getId() {
        return id;
    }

    public String getRole() {
        return role;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public WorkMode getWorkMode() {
        return workMode;
    }

    public Integer getPriority() {
        return priority;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public String getJobUrl() {
        return jobUrl;
    }

    public String getSalaryRange() {
        return salaryRange;
    }

    public String getContactName() {
        return contactName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public User getUser() {
        return user;
    }

    public Company getCompany() {
        return company;
    }
}
