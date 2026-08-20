package com.slotsapi.repository;

import com.slotsapi.model.Booking;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    @Query("SELECT DISTINCT b FROM Booking b JOIN b.resources r " +
            "WHERE b.company.id = :companyId AND r.id IN :resourceIds " +
            "AND b.status = 'CONFIRMED' AND b.startTime < :end AND b.endTime > :start")
    List<Booking> findBookingsForResources(@Param("companyId") Long companyId,
                                           @Param("resourceIds") Set<Long> resourceIds,
                                           @Param("start") OffsetDateTime start,
                                           @Param("end") OffsetDateTime end);
}
