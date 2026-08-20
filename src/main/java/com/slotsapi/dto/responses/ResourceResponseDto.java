package com.slotsapi.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ResourceResponseDto {
    private Long id;
    private String name;
    private String type;
    private String timezone;
    private List<com.slotsapi.dto.IntervalResponseDto> availabilityIntervals;
}