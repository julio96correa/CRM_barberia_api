package com.xclusive.barber.controller;

import com.xclusive.barber.dto.ApiResponse;
import com.xclusive.barber.dto.service.ServiceRequest;
import com.xclusive.barber.dto.service.ServiceResponse;
import com.xclusive.barber.service.ServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
@Tag(name = "Services", description = "Barbershop services management")
public class ServiceController {

    private final ServiceService serviceService;

    @Operation(summary = "Get active services (all roles)", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceResponse>>> getActiveServices() {
        return ResponseEntity.ok(ApiResponse.ok("Active services retrieved", serviceService.getActiveServices()));
    }

    @Operation(summary = "Get all services including inactive (ADMIN only)", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ServiceResponse>>> getAllServices() {
        return ResponseEntity.ok(ApiResponse.ok("All services retrieved", serviceService.getAllServices()));
    }

    @Operation(summary = "Create a new service (ADMIN only)", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ServiceResponse>> createService(@RequestBody ServiceRequest request) {
        ServiceResponse response = serviceService.createService(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Service created", response));
    }

    @Operation(summary = "Update a service (ADMIN only)", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ServiceResponse>> updateService(@PathVariable Long id,
                                                                       @RequestBody ServiceRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Service updated", serviceService.updateService(id, request)));
    }

    @Operation(summary = "Soft delete a service (ADMIN only)", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateService(@PathVariable Long id) {
        serviceService.deactivateService(id);
        return ResponseEntity.ok(ApiResponse.ok("Service deactivated", null));
    }
}
