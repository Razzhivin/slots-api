package com.slotsapi.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class BookingResponseDto {
    private UUID id;
    private String status;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private List<Long> resourceIds;
}