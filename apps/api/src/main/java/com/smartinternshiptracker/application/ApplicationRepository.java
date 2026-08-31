package com.smartinternshiptracker.application;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApplicationRepository extends JpaRepository<Application, String> {

    List<Application> findByUserIdOrderByPriorityAscUpdatedAtDesc(String userId);

    List<Application> findByUserIdAndStatusOrderByPriorityAscUpdatedAtDesc(String userId, ApplicationStatus status);

    Optional<Application> findByIdAndUserId(String id, String userId);

    @Query("""
            select application
            from Application application
            join application.company company
            where application.user.id = :userId
              and (
                  lower(application.role) like lower(concat('%', :search, '%'))
                  or lower(company.name) like lower(concat('%', :search, '%'))
                  or lower(company.location) like lower(concat('%', :search, '%'))
              )
            order by application.priority asc, application.updatedAt desc
            """)
    List<Application> searchApplications(
            @Param("userId") String userId,
            @Param("search") String search
    );

    @Query("""
            select application
            from Application application
            join application.company company
            where application.user.id = :userId
              and application.status = :status
              and (
                  lower(application.role) like lower(concat('%', :search, '%'))
                  or lower(company.name) like lower(concat('%', :search, '%'))
                  or lower(company.location) like lower(concat('%', :search, '%'))
              )
            order by application.priority asc, application.updatedAt desc
            """)
    List<Application> searchApplicationsByStatus(
            @Param("userId") String userId,
            @Param("status") ApplicationStatus status,
            @Param("search") String search
    );
}
