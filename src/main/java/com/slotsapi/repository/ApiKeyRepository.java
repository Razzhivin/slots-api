package com.slotsapi.repository;

import com.slotsapi.model.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, String> {

    @Query("SELECT ak.company.id FROM ApiKey ak WHERE ak.keyToken = :token AND ak.isActive = true")
    Optional<Long> findCompanyIdByToken(@Param("token") String token);
}