package com.xclusive.barber.dto.client;

import com.xclusive.barber.enums.ClientTier;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ClientResponse {
    private Long id;
    private String email;
    private String phone;
    private String notes;
    private Integer loyaltyPoints;
    private ClientTier tier;
    private LocalDateTime lastCompletedAt;
}
