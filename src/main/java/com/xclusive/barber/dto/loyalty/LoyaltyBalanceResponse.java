package com.xclusive.barber.dto.loyalty;

import com.xclusive.barber.enums.ClientTier;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoyaltyBalanceResponse {
    private Long clientId;
    private Integer loyaltyPoints;
    private ClientTier tier;
}
