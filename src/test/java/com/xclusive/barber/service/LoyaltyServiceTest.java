package com.xclusive.barber.service;

import com.xclusive.barber.entity.ClientProfile;
import com.xclusive.barber.entity.LoyaltyTransaction;
import com.xclusive.barber.entity.User;
import com.xclusive.barber.enums.ClientTier;
import com.xclusive.barber.enums.Role;
import com.xclusive.barber.exception.InvalidOperationException;
import com.xclusive.barber.repository.ClientProfileRepository;
import com.xclusive.barber.repository.LoyaltyTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoyaltyServiceTest {

    @Mock ClientProfileRepository clientProfileRepository;
    @Mock LoyaltyTransactionRepository loyaltyTransactionRepository;

    @InjectMocks LoyaltyService loyaltyService;

    private ClientProfile buildClient(int points) {
        User user = User.builder().id(1L).email("client@test.com").role(Role.CLIENT).active(true).build();
        return ClientProfile.builder()
                .id(1L).user(user).phone("3001234567")
                .loyaltyPoints(points).tier(ClientTier.NEW).build();
    }

    @Test
    void testAwardPoints() {
        ClientProfile client = buildClient(50);
        when(clientProfileRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientProfileRepository.save(any())).thenReturn(client);
        when(loyaltyTransactionRepository.save(any())).thenReturn(null);

        loyaltyService.awardPoints(1L, 20);

        assertThat(client.getLoyaltyPoints()).isEqualTo(70);

        ArgumentCaptor<LoyaltyTransaction> txCaptor = ArgumentCaptor.forClass(LoyaltyTransaction.class);
        verify(loyaltyTransactionRepository).save(txCaptor.capture());
        LoyaltyTransaction tx = txCaptor.getValue();
        assertThat(tx.getPointsChange()).isEqualTo(20);
        assertThat(tx.getReason()).isEqualTo("Cita completada");
    }

    @Test
    void testRedeemPoints_sufficient() {
        ClientProfile client = buildClient(100);
        when(clientProfileRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientProfileRepository.save(any())).thenReturn(client);
        when(loyaltyTransactionRepository.save(any())).thenReturn(null);

        loyaltyService.redeemPoints(1L, 30, "Descuento en corte");

        assertThat(client.getLoyaltyPoints()).isEqualTo(70);

        ArgumentCaptor<LoyaltyTransaction> txCaptor = ArgumentCaptor.forClass(LoyaltyTransaction.class);
        verify(loyaltyTransactionRepository).save(txCaptor.capture());
        assertThat(txCaptor.getValue().getPointsChange()).isEqualTo(-30);
        assertThat(txCaptor.getValue().getReason()).isEqualTo("Descuento en corte");
    }

    @Test
    void testRedeemPoints_insufficient() {
        ClientProfile client = buildClient(10);
        when(clientProfileRepository.findById(1L)).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> loyaltyService.redeemPoints(1L, 50, "Descuento"))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("insuficiente");

        verify(clientProfileRepository, never()).save(any());
        verify(loyaltyTransactionRepository, never()).save(any());
    }
}
