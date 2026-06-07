package com.xclusive.barber.controller;

import com.xclusive.barber.dto.ApiResponse;
import com.xclusive.barber.dto.appointment.AppointmentResponse;
import com.xclusive.barber.dto.client.ClientResponse;
import com.xclusive.barber.dto.client.ClientUpdateRequest;
import com.xclusive.barber.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'BARBER')")
@Tag(name = "Clients", description = "Client management")
@SecurityRequirement(name = "bearerAuth")
public class ClientController {

    private final ClientService clientService;

    @Operation(summary = "Get all clients (paginated)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Clients retrieved"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ClientResponse>>> getAllClients(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Clients retrieved", clientService.getAllClients(pageable)));
    }

    @Operation(summary = "Get client by ID")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Client found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Client not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClientResponse>> getClientById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Client retrieved", clientService.getClientById(id)));
    }

    @Operation(summary = "Update client notes or phone")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Client updated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Client not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClientResponse>> updateClient(
            @PathVariable Long id, @RequestBody ClientUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Client updated", clientService.updateClient(id, request)));
    }

    @Operation(summary = "Get completed appointment history for a client")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "History retrieved"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Client not found")
    })
    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getClientHistory(
            @PathVariable Long id, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("History retrieved", clientService.getClientHistory(id, pageable)));
    }

    @Operation(summary = "Get clients inactive for more than 21 days")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Inactive clients retrieved")
    })
    @GetMapping("/inactive")
    public ResponseEntity<ApiResponse<List<ClientResponse>>> getInactiveClients() {
        return ResponseEntity.ok(ApiResponse.ok("Inactive clients", clientService.getInactiveClients()));
    }
}
