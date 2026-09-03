package com.slotsapi.repository;

import com.slotsapi.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByAmocrmSubdomain(String subdomain);
    Optional<Company> findByAmocrmAccountId(String accountId);
    Optional<Company> findByIdAndIsActiveTrue(Long id);
}