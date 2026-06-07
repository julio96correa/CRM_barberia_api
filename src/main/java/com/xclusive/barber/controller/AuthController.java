package com.xclusive.barber.controller;

import com.xclusive.barber.dto.ApiResponse;
import com.xclusive.barber.dto.auth.AuthResponse;
import com.xclusive.barber.dto.auth.LoginRequest;
import com.xclusive.barber.dto.auth.RegisterBarberRequest;
import com.xclusive.barber.dto.auth.RegisterClientRequest;
import com.xclusive.barber.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication endpoints")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Login with email and password")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        AuthResponse data = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", data));
    }

    @Operation(summary = "Register a new client")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Client registered"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email or phone already in use")
    })
    @PostMapping("/register/client")
    public ResponseEntity<ApiResponse<AuthResponse>> registerClient(@RequestBody RegisterClientRequest request) {
        AuthResponse data = authService.registerClient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Client registered successfully", data));
    }

    @Operation(summary = "Register a new barber (ADMIN only)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Barber registered"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email already in use")
    })
    @PostMapping("/register/barber")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AuthResponse>> registerBarber(@RequestBody RegisterBarberRequest request) {
        AuthResponse data = authService.registerBarber(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Barber registered successfully", data));
    }
}
