package com.slotsapi.repository;

import com.slotsapi.model.AmoToken;
import com.slotsapi.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AmoTokenRepository extends JpaRepository<AmoToken, Long> {
    Optional<AmoToken> findByCompanyAndAccountId(Company company, String accountId);
    Optional<AmoToken> findByCompanyId(Long companyId);

    @Query("SELECT t FROM AmoToken t WHERE t.expiresAt < :threshold")
    List<AmoToken> findExpiredBefore(Instant threshold);

    @Modifying
    @Transactional
    @Query("DELETE FROM AmoToken t WHERE t.company.id = :companyId")
    void deleteByCompanyId(Long companyId);
}