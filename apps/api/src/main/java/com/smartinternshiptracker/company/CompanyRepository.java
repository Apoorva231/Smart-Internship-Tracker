package com.smartinternshiptracker.company;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, String> {

    Optional<Company> findByNameAndLocation(String name, String location);
    List<Company> findAllByOrderByLocationAscNameAsc();
}
