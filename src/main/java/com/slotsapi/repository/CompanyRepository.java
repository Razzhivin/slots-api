package com.slotsapi.repository;

import com.slotsapi.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByAmocrmSubdomain(String subdomain);

    @Query("SELECT c.isActive FROM Company c WHERE c.id = :companyId")
    Optional<Boolean> findActiveStatusById(Long companyId);
}