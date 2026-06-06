package com.xclusive.barber.controller;

import com.xclusive.barber.dto.ApiResponse;
import com.xclusive.barber.dto.appointment.AppointmentRequest;
import com.xclusive.barber.dto.appointment.AppointmentResponse;
import com.xclusive.barber.dto.appointment.AppointmentStatusRequest;
import com.xclusive.barber.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Appointment management")
@SecurityRequirement(name = "bearerAuth")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Operation(summary = "Create a new appointment")
    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentResponse>> create(@RequestBody AppointmentRequest request) {
        AppointmentResponse data = appointmentService.createAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Appointment created", data));
    }

    @Operation(summary = "Get appointments by date (ADMIN/BARBER)")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBER')")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.ok("Appointments", appointmentService.getAppointmentsByDate(date)));
    }

    @Operation(summary = "Get appointments for the current week (ADMIN/BARBER)")
    @GetMapping("/week")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBER')")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getByWeek(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return ResponseEntity.ok(ApiResponse.ok("Week appointments", appointmentService.getAppointmentsByWeek(weekStart)));
    }

    @Operation(summary = "Get my pending appointments (CLIENT)")
    @GetMapping("/my")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getMyAppointments(
            @RequestParam Long clientId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("My appointments", appointmentService.getMyAppointments(clientId, pageable)));
    }

    @Operation(summary = "Update appointment status (ADMIN/BARBER)")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBER')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateStatus(
            @PathVariable Long id, @RequestBody AppointmentStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated", appointmentService.updateStatus(id, request.status())));
    }

    @Operation(summary = "Cancel an appointment")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancel(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);
        return ResponseEntity.ok(ApiResponse.ok("Appointment cancelled", null));
    }
}
