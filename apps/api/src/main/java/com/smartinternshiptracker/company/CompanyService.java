package com.smartinternshiptracker.company;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public List<CompanyResponse> listCompanies() {
        return companyRepository.findAllByOrderByLocationAscNameAsc().stream()
                .map(CompanyResponse::from)
                .toList();
    }
}
