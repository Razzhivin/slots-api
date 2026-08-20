package com.slotsapi.repository;

import com.slotsapi.model.Resource;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
    List<Resource> findByCompanyId(Long companyId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Resource> findByIdInAndCompanyId(Set<Long> ids, Long companyId);

    @Query("SELECT COUNT(b) FROM Booking b JOIN b.resources r " +
            "WHERE b.company.id = :companyId AND r.id IN :resourceIds " +
            "AND b.status = 'CONFIRMED' AND b.startTime < :endTime AND b.endTime > :startTime")
    long countOverlappingBookings(@Param("companyId") Long companyId,
                                  @Param("resourceIds") Set<Long> resourceIds,
                                  @Param("startTime") OffsetDateTime startTime,
                                  @Param("endTime") OffsetDateTime endTime);
}