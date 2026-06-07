package com.xclusive.barber.service;

import com.xclusive.barber.dto.appointment.AppointmentResponse;
import com.xclusive.barber.dto.client.ClientResponse;
import com.xclusive.barber.dto.dashboard.DashboardSummaryResponse;
import com.xclusive.barber.dto.dashboard.TodayResponse;
import com.xclusive.barber.dto.dashboard.TopServiceResponse;
import com.xclusive.barber.entity.ClientProfile;
import com.xclusive.barber.enums.ClientTier;
import com.xclusive.barber.repository.AppointmentRepository;
import com.xclusive.barber.repository.ClientProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AppointmentRepository appointmentRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final AppointmentService appointmentService;

    public DashboardSummaryResponse getSummary() {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        Map<String, Long> tierDistribution = Map.of(
                ClientTier.NEW.name(), clientProfileRepository.countByTier(ClientTier.NEW),
                ClientTier.REGULAR.name(), clientProfileRepository.countByTier(ClientTier.REGULAR),
                ClientTier.VIP.name(), clientProfileRepository.countByTier(ClientTier.VIP)
        );

        return DashboardSummaryResponse.builder()
                .totalClients(clientProfileRepository.count())
                .appointmentsToday(appointmentRepository.countByAppointmentDate(LocalDate.now()))
                .newClientsThisMonth(clientProfileRepository.countNewSince(startOfMonth))
                .tierDistribution(tierDistribution)
                .build();
    }

    public TodayResponse getToday() {
        LocalDate today = LocalDate.now();
        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByDate(today);
        return TodayResponse.builder()
                .date(today)
                .appointments(appointments)
                .build();
    }

    public List<TopServiceResponse> getTopServices() {
        List<Object[]> rows = appointmentRepository.findTopServices(PageRequest.of(0, 5));
        int[] rank = {1};
        return rows.stream()
                .map(row -> TopServiceResponse.builder()
                        .serviceId((Long) row[0])
                        .serviceName((String) row[1])
                        .totalAppointments((Long) row[2])
                        .rank(rank[0]++)
                        .build())
                .toList();
    }

    public List<ClientResponse> getInactiveClients() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(21);
        return clientProfileRepository.findInactiveSince(cutoff).stream()
                .map(c -> ClientResponse.builder()
                        .id(c.getId())
                        .email(c.getUser().getEmail())
                        .phone(c.getPhone())
                        .notes(c.getNotes())
                        .loyaltyPoints(c.getLoyaltyPoints())
                        .tier(c.getTier())
                        .lastCompletedAt(c.getLastCompletedAt())
                        .build())
                .toList();
    }
}
