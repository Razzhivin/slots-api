package com.slotsapi.controller;

import com.slotsapi.dto.*;
import com.slotsapi.dto.BookingResponseDto;
import com.slotsapi.dto.ResourceResponseDto;
import com.slotsapi.service.BookingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/resources")
    public ResponseEntity<ResourceResponseDto> createResource(
            @RequestAttribute("CURRENT_COMPANY_ID") Long companyId,
            @RequestBody Map<String, String> request) {
        ResourceResponseDto created = bookingService.createResource(companyId, request.get("name"), request.get("type"), request.get("timezone"));
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
            @RequestBody Map<String, String> request) {
        ResourceResponseDto updated = bookingService.updateResource(companyId, id, request.get("name"), request.get("type"), request.get("timezone"));
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/resources/{id}")
    public ResponseEntity<Void> removeResource(
            @RequestAttribute("CURRENT_COMPANY_ID") Long companyId,
            @PathVariable Long id) {
        bookingService.deleteResource(companyId, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/resources/{resourceId}/intervals")
    public ResponseEntity<ResourceResponseDto> addInterval(
            @RequestAttribute("CURRENT_COMPANY_ID") Long companyId,
            @PathVariable Long resourceId,
            @RequestBody Map<String, String> request) {
        ResourceResponseDto updated = bookingService.addAvailabilityInterval(companyId, resourceId, request.get("day_of_week"), request.get("start_time"), request.get("end_time"));
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/resources/{id}/schedule")
    public ResponseEntity<ResourceResponseDto> overrideSchedule(
            @RequestAttribute("CURRENT_COMPANY_ID") Long companyId,
            @PathVariable Long id,
            @RequestBody List<Map<String, String>> request) {
        ResourceResponseDto updated = bookingService.updateResourceSchedule(companyId, id, request);
        return ResponseEntity.ok(updated);
    }

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
            @RequestBody Map<String, Object> request) {
        List<Integer> ids = (List<Integer>) request.get("resource_ids");
        Set<Long> rIds = new java.util.HashSet<>();
        for (Integer id : ids) { rIds.add(id.longValue()); }
        OffsetDateTime start = OffsetDateTime.parse((String) request.get("start_time"));
        OffsetDateTime end = OffsetDateTime.parse((String) request.get("end_time"));
        return ResponseEntity.ok(bookingService.createBooking(companyId, rIds, start, end));
    }
}
