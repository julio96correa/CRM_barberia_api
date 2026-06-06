package com.xclusive.barber.controller;

import com.xclusive.barber.dto.ApiResponse;
import com.xclusive.barber.dto.loyalty.LoyaltyBalanceResponse;
import com.xclusive.barber.dto.loyalty.LoyaltyTransactionResponse;
import com.xclusive.barber.dto.loyalty.RedeemRequest;
import com.xclusive.barber.service.LoyaltyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/loyalty")
@RequiredArgsConstructor
@Tag(name = "Loyalty", description = "Loyalty points management")
@SecurityRequirement(name = "bearerAuth")
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    @Operation(summary = "Get loyalty balance for a client")
    @GetMapping("/{clientId}/balance")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<ApiResponse<LoyaltyBalanceResponse>> getBalance(@PathVariable Long clientId) {
        return ResponseEntity.ok(ApiResponse.ok("Balance", loyaltyService.getBalance(clientId)));
    }

    @Operation(summary = "Redeem loyalty points (ADMIN)")
    @PostMapping("/{clientId}/redeem")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> redeem(
            @PathVariable Long clientId, @RequestBody RedeemRequest request) {
        loyaltyService.redeemPoints(clientId, request.points(), request.reason());
        return ResponseEntity.ok(ApiResponse.ok("Points redeemed", null));
    }

    @Operation(summary = "Get transaction history for a client")
    @GetMapping("/{clientId}/transactions")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<ApiResponse<Page<LoyaltyTransactionResponse>>> getTransactions(
            @PathVariable Long clientId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Transactions", loyaltyService.getTransactions(clientId, pageable)));
    }
}
