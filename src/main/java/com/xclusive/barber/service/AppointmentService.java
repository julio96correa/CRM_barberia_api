package com.xclusive.barber.service;

import com.xclusive.barber.dto.appointment.AppointmentRequest;
import com.xclusive.barber.dto.appointment.AppointmentResponse;
import com.xclusive.barber.entity.Appointment;
import com.xclusive.barber.entity.BarberProfile;
import com.xclusive.barber.entity.ClientProfile;
import com.xclusive.barber.entity.Service;
import com.xclusive.barber.enums.AppointmentStatus;
import com.xclusive.barber.exception.InvalidOperationException;
import com.xclusive.barber.exception.ResourceNotFoundException;
import com.xclusive.barber.repository.AppointmentRepository;
import com.xclusive.barber.repository.BarberProfileRepository;
import com.xclusive.barber.repository.ClientProfileRepository;
import com.xclusive.barber.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final BarberProfileRepository barberProfileRepository;
    private final ServiceRepository serviceRepository;
    private final LoyaltyService loyaltyService;

    @Transactional
    public AppointmentResponse createAppointment(AppointmentRequest request) {
        if (appointmentRepository.existsByBarberProfileIdAndAppointmentDateAndStartHour(
                request.barberId(), request.appointmentDate(), request.startHour())) {
            throw new InvalidOperationException("Slot no disponible");
        }

        ClientProfile client = clientProfileRepository.findById(request.clientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + request.clientId()));
        BarberProfile barber = barberProfileRepository.findById(request.barberId())
                .orElseThrow(() -> new ResourceNotFoundException("Barber not found: " + request.barberId()));
        Service service = serviceRepository.findById(request.serviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + request.serviceId()));

        Appointment appointment = Appointment.builder()
                .clientProfile(client)
                .barberProfile(barber)
                .service(service)
                .appointmentDate(request.appointmentDate())
                .startHour(request.startHour())
                .status(AppointmentStatus.PENDING)
                .build();

        return toResponse(appointmentRepository.save(appointment));
    }

    public List<AppointmentResponse> getAppointmentsByDate(LocalDate date) {
        return appointmentRepository.findByAppointmentDate(date).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AppointmentResponse> getAppointmentsByWeek(LocalDate weekStart) {
        return appointmentRepository.findByAppointmentDateBetween(weekStart, weekStart.plusDays(6)).stream()
                .map(this::toResponse)
                .toList();
    }

    public Page<AppointmentResponse> getMyAppointments(Long clientId, Pageable pageable) {
        return appointmentRepository.findByClientProfileIdAndStatus(clientId, AppointmentStatus.PENDING, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public AppointmentResponse updateStatus(Long id, AppointmentStatus newStatus) {
        Appointment appointment = findById(id);
        appointment.setStatus(newStatus);

        if (newStatus == AppointmentStatus.COMPLETED) {
            ClientProfile client = appointment.getClientProfile();
            client.setLastCompletedAt(LocalDateTime.now());

            int points = appointment.getService().getPointsValue();
            if (points > 0) {
                loyaltyService.awardPoints(client.getId(), points);
            }

            long completed = appointmentRepository.countByClientProfileIdAndStatus(
                    client.getId(), AppointmentStatus.COMPLETED);
            client.setTier(LoyaltyService.recalculateTier(completed + 1));
        }

        return toResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    public void cancelAppointment(Long id) {
        Appointment appointment = findById(id);
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new InvalidOperationException("Solo se pueden cancelar citas en estado PENDING");
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }

    private Appointment findById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));
    }

    private AppointmentResponse toResponse(Appointment a) {
        return AppointmentResponse.builder()
                .id(a.getId())
                .clientName(a.getClientProfile().getUser().getEmail())
                .barberName(a.getBarberProfile().getUser().getEmail())
                .serviceName(a.getService().getName())
                .appointmentDate(a.getAppointmentDate())
                .startHour(a.getStartHour())
                .status(a.getStatus())
                .build();
    }
}
