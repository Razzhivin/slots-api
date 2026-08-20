package com.slotsapi.service;

import com.slotsapi.dto.*;
import com.slotsapi.dto.BookingResponseDto;
import com.slotsapi.dto.IntervalResponseDto;
import com.slotsapi.dto.ResourceResponseDto;
import com.slotsapi.model.*;
import com.slotsapi.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookingService {
    private final ResourceRepository resourceRepository;
    private final BookingRepository bookingRepository;
    private final CompanyRepository companyRepository;

    public BookingService(ResourceRepository rr, BookingRepository br, CompanyRepository cr) {
        this.resourceRepository = rr;
        this.bookingRepository = br;
        this.companyRepository = cr;
    }

    @Transactional
    public ResourceResponseDto createResource(Long companyId, String name, String type, String timezone) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found."));
        Resource resource = new Resource();
        resource.setName(name);
        resource.setType(type);
        resource.setTimezone(timezone != null ? timezone : "UTC");
        resource.setCompany(company);
        return mapToResourceDto(resourceRepository.save(resource));
    }

    @Transactional
    public ResourceResponseDto addAvailabilityInterval(Long companyId, Long resourceId, String dayOfWeekStr, String startTimeStr, String endTimeStr) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found."));
        if (!resource.getCompany().getId().equals(companyId)) {
            throw new SecurityException("Access denied: Resource belongs to another company.");
        }
        ResourceAvailabilityInterval interval = new ResourceAvailabilityInterval();
        interval.setResource(resource);
        interval.setDayOfWeek(DayOfWeek.valueOf(dayOfWeekStr.toUpperCase()));
        interval.setStartTime(LocalTime.parse(startTimeStr));
        interval.setEndTime(LocalTime.parse(endTimeStr));
        resource.getAvailabilityIntervals().add(interval);
        return mapToResourceDto(resourceRepository.save(resource));
    }

    @Transactional
    public BookingResponseDto createBooking(Long companyId, Set<Long> resourceIds, OffsetDateTime start, OffsetDateTime end) {
        List<Resource> lockedResources = resourceRepository.findByIdInAndCompanyId(resourceIds, companyId);
        if (lockedResources.size() != resourceIds.size()) {
            throw new IllegalArgumentException("One or more resources not found or belong to another company.");
        }
        for (Resource resource : lockedResources) {
            if (!isResourceAvailableInItsSchedule(resource, start, end)) {
                throw new IllegalStateException("Resource " + resource.getName() + " is not working.");
            }
        }
        if (resourceRepository.countOverlappingBookings(companyId, resourceIds, start, end) > 0) {
            throw new IllegalStateException("SLOT_ALREADY_BOOKED");
        }
        Company company = companyRepository.findById(companyId).orElseThrow();
        Booking booking = new Booking();
        booking.setStartTime(start);
        booking.setEndTime(end);
        booking.setCompany(company);
        booking.setResources(new HashSet<>(lockedResources));
        return mapToBookingDto(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    public List<ResourceResponseDto> getAllResources(Long companyId) {
        return resourceRepository.findByCompanyId(companyId).stream()
                .map(this::mapToResourceDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ResourceResponseDto getResourceById(Long companyId, Long resourceId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));
        if (!resource.getCompany().getId().equals(companyId)) throw new SecurityException("Access denied");
        return mapToResourceDto(resource);
    }

    @Transactional
    public ResourceResponseDto updateResource(Long companyId, Long resourceId, String name, String type, String timezone) {
        ResourceResponseDto dto = getResourceById(companyId, resourceId);
        Resource resource = resourceRepository.findById(resourceId).orElseThrow();
        if (name != null) resource.setName(name);
        if (type != null) resource.setType(type);
        if (timezone != null) resource.setTimezone(timezone);
        return mapToResourceDto(resourceRepository.save(resource));
    }

    @Transactional
    public void deleteResource(Long companyId, Long resourceId) {
        ResourceResponseDto dto = getResourceById(companyId, resourceId);
        resourceRepository.deleteById(resourceId);
    }

    @Transactional
    public ResourceResponseDto updateResourceSchedule(Long companyId, Long resourceId, List<Map<String, String>> newIntervals) {
        ResourceResponseDto dto = getResourceById(companyId, resourceId);
        Resource resource = resourceRepository.findById(resourceId).orElseThrow();
        resource.getAvailabilityIntervals().clear();
        for (Map<String, String> intervalData : newIntervals) {
            ResourceAvailabilityInterval interval = new ResourceAvailabilityInterval();
            interval.setResource(resource);
            interval.setDayOfWeek(DayOfWeek.valueOf(intervalData.get("day_of_week").toUpperCase()));
            interval.setStartTime(LocalTime.parse(intervalData.get("start_time")));
            interval.setEndTime(LocalTime.parse(intervalData.get("end_time")));
            resource.getAvailabilityIntervals().add(interval);
        }
        return mapToResourceDto(resourceRepository.save(resource));
    }

    public List<Map<String, OffsetDateTime>> getAvailableSlots(Long companyId, Set<Long> resourceIds, LocalDate date, int slotDurationMinutes) {
        List<Resource> resources = resourceRepository.findAllById(resourceIds);
        if (resources.isEmpty()) return Collections.emptyList();
        boolean isOwner = resources.stream().allMatch(r -> r.getCompany().getId().equals(companyId));
        if (!isOwner) throw new SecurityException("Access denied");

        ZoneId utc = ZoneId.of("UTC");
        OffsetDateTime dayStart = date.atStartOfDay().atZone(utc).toOffsetDateTime();
        OffsetDateTime dayEnd = date.atTime(LocalTime.MAX).atZone(utc).toOffsetDateTime();
        List<Booking> activeBookings = bookingRepository.findBookingsForResources(companyId, resourceIds, dayStart, dayEnd);
        List<Map<String, OffsetDateTime>> targetSlots = new ArrayList<>();
        OffsetDateTime currentIntervalStart = dayStart;
        while (currentIntervalStart.plusMinutes(slotDurationMinutes).isBefore(dayEnd)) {
            OffsetDateTime currentIntervalEnd = currentIntervalStart.plusMinutes(slotDurationMinutes);
            final OffsetDateTime startCheck = currentIntervalStart;
            final OffsetDateTime endCheck = currentIntervalEnd;
            boolean allWorking = resources.stream().allMatch(r -> isResourceAvailableInItsSchedule(r, startCheck, endCheck));
            boolean hasConflict = activeBookings.stream().anyMatch(b -> b.getStartTime().isBefore(endCheck) && b.getEndTime().isAfter(startCheck));
            if (allWorking && !hasConflict) {
                Map<String, OffsetDateTime> slot = new HashMap<>();
                slot.put("start_time", startCheck);
                slot.put("end_time", endCheck);
                targetSlots.add(slot);
            }
            currentIntervalStart = currentIntervalStart.plusMinutes(slotDurationMinutes);
        }
        return targetSlots;
    }

    // --- МАППЕРЫ ---
    private ResourceResponseDto mapToResourceDto(Resource resource) {
        ResourceResponseDto dto = new ResourceResponseDto();
        dto.setId(resource.getId());
        dto.setName(resource.getName());
        dto.setType(resource.getType());
        dto.setTimezone(resource.getTimezone());
        dto.setAvailabilityIntervals(resource.getAvailabilityIntervals().stream().map(i -> {
            IntervalResponseDto idto = new IntervalResponseDto();
            idto.setId(i.getId());
            idto.setDayOfWeek(i.getDayOfWeek());
            idto.setStartTime(i.getStartTime());
            idto.setEndTime(i.getEndTime());
            return idto;
        }).collect(Collectors.toList()));
        return dto;
    }

    private BookingResponseDto mapToBookingDto(Booking booking) {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(booking.getId());
        dto.setStatus(booking.getStatus());
        dto.setStartTime(booking.getStartTime());
        dto.setEndTime(booking.getEndTime());
        dto.setResourceIds(booking.getResources().stream().map(Resource::getId).collect(Collectors.toList()));
        return dto;
    }

    private boolean isResourceAvailableInItsSchedule(Resource resource, OffsetDateTime startUtc, OffsetDateTime endUtc) {
        ZoneId resourceZone = ZoneId.of(resource.getTimezone());
        ZonedDateTime localStart = startUtc.atZoneSameInstant(resourceZone);
        ZonedDateTime localEnd = endUtc.atZoneSameInstant(resourceZone);
        DayOfWeek startDay = localStart.getDayOfWeek();
        return resource.getAvailabilityIntervals().stream()
                .filter(i -> i.getDayOfWeek() == startDay)
                .anyMatch(i -> !localStart.toLocalTime().isBefore(i.getStartTime()) && !localEnd.toLocalTime().isAfter(i.getEndTime()));
    }
}
