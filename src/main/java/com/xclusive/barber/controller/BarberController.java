package com.xclusive.barber.controller;

import com.xclusive.barber.dto.ApiResponse;
import com.xclusive.barber.dto.barber.BarberAvailabilityResponse;
import com.xclusive.barber.dto.barber.BarberResponse;
import com.xclusive.barber.dto.barber.BarberScheduleRequest;
import com.xclusive.barber.service.BarberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/barbers")
@RequiredArgsConstructor
@Tag(name = "Barbers", description = "Barber management and scheduling")
public class BarberController {

    private final BarberService barberService;

    @Operation(summary = "Get all barbers (all roles)", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ResponseEntity<ApiResponse<List<BarberResponse>>> getAllBarbers() {
        return ResponseEntity.ok(ApiResponse.ok("Barbers retrieved", barberService.getAllBarbers()));
    }

    @Operation(summary = "Get barber by ID (all roles)", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BarberResponse>> getBarberById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Barber retrieved", barberService.getBarberById(id)));
    }

    @Operation(summary = "Update barber schedule (ADMIN only)", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}/schedule")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateSchedule(@PathVariable Long id,
                                                             @RequestBody List<BarberScheduleRequest> requests) {
        barberService.updateSchedule(id, requests);
        return ResponseEntity.ok(ApiResponse.ok("Schedule updated", null));
    }

    @Operation(summary = "Mark a day off for a barber (ADMIN only)", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}/day-off")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> markDayOff(@PathVariable Long id,
                                                         @RequestBody Map<String, String> body) {
        LocalDate date = LocalDate.parse(body.get("date"));
        barberService.markDayOff(id, date);
        return ResponseEntity.ok(ApiResponse.ok("Day off marked", null));
    }

    @Operation(summary = "Get available slots for a barber on a date (all roles)", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<BarberAvailabilityResponse>> getAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.ok("Availability retrieved", barberService.getAvailableSlots(id, date)));
    }
}
