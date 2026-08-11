package com.smartinternshiptracker.application;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, String> {

    List<Application> findByUserIdOrderByPriorityAscUpdatedAtDesc(String userId);
}
