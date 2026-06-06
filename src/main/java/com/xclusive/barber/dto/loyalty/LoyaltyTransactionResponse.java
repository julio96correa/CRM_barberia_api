package com.xclusive.barber.dto.loyalty;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LoyaltyTransactionResponse {
    private Long id;
    private Integer pointsChange;
    private String reason;
    private LocalDateTime createdAt;
}
