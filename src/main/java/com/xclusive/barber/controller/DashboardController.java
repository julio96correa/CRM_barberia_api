package com.xclusive.barber.controller;

import com.xclusive.barber.dto.ApiResponse;
import com.xclusive.barber.dto.dashboard.DashboardSummaryResponse;
import com.xclusive.barber.dto.dashboard.TodayResponse;
import com.xclusive.barber.dto.dashboard.TopServiceResponse;
import com.xclusive.barber.entity.ClientProfile;
import com.xclusive.barber.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'BARBER')")
@Tag(name = "Dashboard", description = "Admin/Barber dashboard statistics")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Get dashboard summary")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary() {
        return ResponseEntity.ok(ApiResponse.ok("Summary", dashboardService.getSummary()));
    }

    @Operation(summary = "Get today's appointments")
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<TodayResponse>> getToday() {
        return ResponseEntity.ok(ApiResponse.ok("Today", dashboardService.getToday()));
    }

    @Operation(summary = "Get top 5 services by appointment count")
    @GetMapping("/top-services")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TopServiceResponse>>> getTopServices() {
        return ResponseEntity.ok(ApiResponse.ok("Top services", dashboardService.getTopServices()));
    }

    @Operation(summary = "Get clients inactive for more than 21 days")
    @GetMapping("/inactive-clients")
    public ResponseEntity<ApiResponse<List<ClientProfile>>> getInactiveClients() {
        return ResponseEntity.ok(ApiResponse.ok("Inactive clients", dashboardService.getInactiveClients()));
    }
}
