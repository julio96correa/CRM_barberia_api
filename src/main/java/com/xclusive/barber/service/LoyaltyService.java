package com.xclusive.barber.service;

import com.xclusive.barber.dto.loyalty.LoyaltyBalanceResponse;
import com.xclusive.barber.dto.loyalty.LoyaltyTransactionResponse;
import com.xclusive.barber.entity.ClientProfile;
import com.xclusive.barber.entity.LoyaltyTransaction;
import com.xclusive.barber.enums.ClientTier;
import com.xclusive.barber.exception.InvalidOperationException;
import com.xclusive.barber.exception.ResourceNotFoundException;
import com.xclusive.barber.repository.ClientProfileRepository;
import com.xclusive.barber.repository.LoyaltyTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoyaltyService {

    private final ClientProfileRepository clientProfileRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;

    @Transactional
    public void awardPoints(Long clientId, int points) {
        ClientProfile client = findClient(clientId);
        client.setLoyaltyPoints(client.getLoyaltyPoints() + points);
        clientProfileRepository.save(client);

        loyaltyTransactionRepository.save(LoyaltyTransaction.builder()
                .clientProfile(client)
                .pointsChange(points)
                .reason("Cita completada")
                .build());
    }

    @Transactional
    public void redeemPoints(Long clientId, int points, String reason) {
        ClientProfile client = findClient(clientId);
        if (client.getLoyaltyPoints() < points) {
            throw new InvalidOperationException("Saldo insuficiente de puntos");
        }
        client.setLoyaltyPoints(client.getLoyaltyPoints() - points);
        clientProfileRepository.save(client);

        loyaltyTransactionRepository.save(LoyaltyTransaction.builder()
                .clientProfile(client)
                .pointsChange(-points)
                .reason(reason)
                .build());
    }

    public LoyaltyBalanceResponse getBalance(Long clientId) {
        ClientProfile client = findClient(clientId);
        return LoyaltyBalanceResponse.builder()
                .clientId(clientId)
                .loyaltyPoints(client.getLoyaltyPoints())
                .tier(client.getTier())
                .build();
    }

    public Page<LoyaltyTransactionResponse> getTransactions(Long clientId, Pageable pageable) {
        return loyaltyTransactionRepository.findByClientProfileId(clientId, pageable)
                .map(t -> LoyaltyTransactionResponse.builder()
                        .id(t.getId())
                        .pointsChange(t.getPointsChange())
                        .reason(t.getReason())
                        .createdAt(t.getCreatedAt())
                        .build());
    }

    private ClientProfile findClient(Long clientId) {
        return clientProfileRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + clientId));
    }

    public static ClientTier recalculateTier(long completedCount) {
        if (completedCount >= 10) return ClientTier.VIP;
        if (completedCount >= 3) return ClientTier.REGULAR;
        return ClientTier.NEW;
    }
}
