package com.xclusive.barber.service;

import com.xclusive.barber.dto.appointment.AppointmentResponse;
import com.xclusive.barber.dto.client.ClientResponse;
import com.xclusive.barber.dto.client.ClientUpdateRequest;
import com.xclusive.barber.entity.ClientProfile;
import com.xclusive.barber.enums.AppointmentStatus;
import com.xclusive.barber.exception.ResourceNotFoundException;
import com.xclusive.barber.repository.AppointmentRepository;
import com.xclusive.barber.repository.ClientProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientProfileRepository clientProfileRepository;
    private final AppointmentRepository appointmentRepository;

    public Page<ClientResponse> getAllClients(Pageable pageable) {
        return clientProfileRepository.findAll(pageable).map(this::toResponse);
    }

    public ClientResponse getClientById(Long id) {
        return toResponse(findClient(id));
    }

    @Transactional
    public ClientResponse updateClient(Long id, ClientUpdateRequest request) {
        ClientProfile client = findClient(id);
        if (request.notes() != null) client.setNotes(request.notes());
        if (request.phone() != null) client.setPhone(request.phone());
        return toResponse(clientProfileRepository.save(client));
    }

    public Page<AppointmentResponse> getClientHistory(Long clientId, Pageable pageable) {
        return appointmentRepository.findByClientProfileIdAndStatus(clientId, AppointmentStatus.COMPLETED, pageable)
                .map(a -> AppointmentResponse.builder()
                        .id(a.getId())
                        .clientName(a.getClientProfile().getUser().getEmail())
                        .barberName(a.getBarberProfile().getUser().getEmail())
                        .serviceName(a.getService().getName())
                        .appointmentDate(a.getAppointmentDate())
                        .startHour(a.getStartHour())
                        .status(a.getStatus())
                        .build());
    }

    public List<ClientResponse> getInactiveClients() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(21);
        return clientProfileRepository.findInactiveSince(cutoff).stream()
                .map(this::toResponse)
                .toList();
    }

    private ClientProfile findClient(Long id) {
        return clientProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + id));
    }

    private ClientResponse toResponse(ClientProfile c) {
        return ClientResponse.builder()
                .id(c.getId())
                .email(c.getUser().getEmail())
                .phone(c.getPhone())
                .notes(c.getNotes())
                .loyaltyPoints(c.getLoyaltyPoints())
                .tier(c.getTier())
                .lastCompletedAt(c.getLastCompletedAt())
                .build();
    }
}
