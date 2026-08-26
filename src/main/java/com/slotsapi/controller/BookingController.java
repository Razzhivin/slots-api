package com.slotsapi.controller;

import com.slotsapi.dto.requests.*;
import com.slotsapi.dto.responses.BookingResponseDto;
import com.slotsapi.dto.responses.ResourceResponseDto;
import com.slotsapi.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    // --- УПРАВЛЕНИЕ РЕСУРСАМИ (CRUD) ---

    @PostMapping("/resources")
    public ResponseEntity<ResourceResponseDto> createResource(
            @RequestAttribute("CURRENT_COMPANY_ID") Long companyId,
            @RequestBody ResourceCreateDto request) {
        ResourceResponseDto created = bookingService.createResource(
                companyId, request.getName(), request.getType(), request.getTimezone()
        );
        return ResponseEntity.ok(created);
    }

    @GetMapping("/resources")
    public ResponseEntity<List<ResourceResponseDto>> listResources(@RequestAttribute("CURRENT_COMPANY_ID") Long companyId) {
        return ResponseEntity.ok(bookingService.getAllResources(companyId));
    }

    @GetMapping("/resources/{id}")
    public ResponseEntity<ResourceResponseDto> getResource(
            @RequestAttribute("CURRENT_COMPANY_ID") Long companyId,
            @PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getResourceById(companyId, id));
    }

    @PutMapping("/resources/{id}")
    public ResponseEntity<ResourceResponseDto> modifyResource(
            @RequestAttribute("CURRENT_COMPANY_ID") Long companyId,
            @PathVariable Long id,
            @RequestBody ResourceUpdateDto request) {
        ResourceResponseDto updated = bookingService.updateResource(
                companyId, id, request.getName(), request.getType(), request.getTimezone()
        );
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/resources/{id}")
    public ResponseEntity<Void> removeResource(
            @RequestAttribute("CURRENT_COMPANY_ID") Long companyId,
            @PathVariable Long id) {
        bookingService.deleteResource(companyId, id);
        return ResponseEntity.noContent().build();
    }

    // --- УПРАВЛЕНИЕ ГРАФИКАМИ И ИНТЕРВАЛАМИ ---

    @PostMapping("/resources/{resourceId}/intervals")
    public ResponseEntity<ResourceResponseDto> addInterval(
            @RequestAttribute("CURRENT_COMPANY_ID") Long companyId,
            @PathVariable Long resourceId,
            @Valid @RequestBody IntervalCreateDto request) {
        ResourceResponseDto updated = bookingService.addAvailabilityInterval(companyId, resourceId, request);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/resources/{id}/schedule")
    public ResponseEntity<ResourceResponseDto> overrideSchedule(
            @RequestAttribute("CURRENT_COMPANY_ID") Long companyId,
            @PathVariable Long id,
            @Valid @RequestBody ScheduleOverrideDto request) {
        ResourceResponseDto updated = bookingService.updateResourceSchedule(companyId, id, request.getIntervals());
        return ResponseEntity.ok(updated);
    }

    // --- БРОНИРОВАНИЕ И СЛОТЫ ---

    @GetMapping("/slots")
    public ResponseEntity<List<Map<String, OffsetDateTime>>> getSlots(
            @RequestAttribute("CURRENT_COMPANY_ID") Long companyId,
            @RequestParam Set<Long> resourceIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "30") int slotDurationMinutes) {
        return ResponseEntity.ok(bookingService.getAvailableSlots(companyId, resourceIds, date, slotDurationMinutes));
    }

    @PostMapping
    public ResponseEntity<BookingResponseDto> createBooking(
            @RequestAttribute("CURRENT_COMPANY_ID") Long companyId,
            @Valid @RequestBody BookingCreateDto request) {

        OffsetDateTime start = OffsetDateTime.parse(request.getStartTime());
        OffsetDateTime end = OffsetDateTime.parse(request.getEndTime());

        BookingResponseDto created = bookingService.createBooking(
                companyId, request.getResourceIds(), start, end
        );
        return ResponseEntity.ok(created);
    }
}
