package com.xclusive.barber.service;

import com.xclusive.barber.dto.appointment.AppointmentRequest;
import com.xclusive.barber.dto.appointment.AppointmentResponse;
import com.xclusive.barber.entity.Appointment;
import com.xclusive.barber.entity.BarberProfile;
import com.xclusive.barber.entity.ClientProfile;
import com.xclusive.barber.entity.Service;
import com.xclusive.barber.entity.User;
import com.xclusive.barber.enums.AppointmentStatus;
import com.xclusive.barber.enums.ClientTier;
import com.xclusive.barber.enums.Role;
import com.xclusive.barber.exception.InvalidOperationException;
import com.xclusive.barber.repository.AppointmentRepository;
import com.xclusive.barber.repository.BarberProfileRepository;
import com.xclusive.barber.repository.ClientProfileRepository;
import com.xclusive.barber.repository.ServiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock AppointmentRepository appointmentRepository;
    @Mock ClientProfileRepository clientProfileRepository;
    @Mock BarberProfileRepository barberProfileRepository;
    @Mock ServiceRepository serviceRepository;
    @Mock LoyaltyService loyaltyService;

    @InjectMocks AppointmentService appointmentService;

    private User buildUser(Long id, String email) {
        return User.builder().id(id).email(email).role(Role.CLIENT).active(true).build();
    }

    private ClientProfile buildClient(Long id) {
        return ClientProfile.builder()
                .id(id).user(buildUser(id, "client@test.com"))
                .phone("3001234567").loyaltyPoints(0).tier(ClientTier.NEW).build();
    }

    private BarberProfile buildBarber(Long id) {
        return BarberProfile.builder()
                .id(id).user(buildUser(id, "barber@test.com"))
                .phone("3009876543").specialty("Corte").build();
    }

    private Service buildService(Long id, int points) {
        return Service.builder().id(id).name("Corte clásico").pointsValue(points).active(true).build();
    }

    @Test
    void testCreateAppointment_success() {
        LocalDate date = LocalDate.of(2025, 7, 1);
        AppointmentRequest request = new AppointmentRequest(1L, 2L, 3L, date, "10:00");

        when(appointmentRepository.existsByBarberProfileIdAndAppointmentDateAndStartHour(2L, date, 10))
                .thenReturn(false);
        when(clientProfileRepository.findById(1L)).thenReturn(Optional.of(buildClient(1L)));
        when(barberProfileRepository.findById(2L)).thenReturn(Optional.of(buildBarber(2L)));
        when(serviceRepository.findById(3L)).thenReturn(Optional.of(buildService(3L, 10)));

        Appointment saved = Appointment.builder()
                .id(99L).clientProfile(buildClient(1L)).barberProfile(buildBarber(2L))
                .service(buildService(3L, 10)).appointmentDate(date).startHour(10)
                .status(AppointmentStatus.PENDING).build();
        when(appointmentRepository.save(any())).thenReturn(saved);

        AppointmentResponse response = appointmentService.createAppointment(request);

        assertThat(response.getId()).isEqualTo(99L);
        assertThat(response.getStatus()).isEqualTo(AppointmentStatus.PENDING);
        verify(appointmentRepository).save(argThat(a -> a.getStatus() == AppointmentStatus.PENDING));
    }

    @Test
    void testCreateAppointment_slotOccupied() {
        LocalDate date = LocalDate.of(2025, 7, 1);
        AppointmentRequest request = new AppointmentRequest(1L, 2L, 3L, date, "10:00");

        when(appointmentRepository.existsByBarberProfileIdAndAppointmentDateAndStartHour(2L, date, 10))
                .thenReturn(true);

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Slot no disponible");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void testCancelAppointment_notPending() {
        Appointment appointment = Appointment.builder()
                .id(1L).clientProfile(buildClient(1L)).barberProfile(buildBarber(2L))
                .service(buildService(3L, 10)).appointmentDate(LocalDate.now()).startHour(10)
                .status(AppointmentStatus.COMPLETED).build();

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.cancelAppointment(1L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    void testUpdateStatus_completed() {
        ClientProfile client = buildClient(1L);
        client.setLoyaltyPoints(0);
        Service service = buildService(3L, 15);

        Appointment appointment = Appointment.builder()
                .id(1L).clientProfile(client).barberProfile(buildBarber(2L))
                .service(service).appointmentDate(LocalDate.now()).startHour(10)
                .status(AppointmentStatus.PENDING).build();

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.countByClientProfileIdAndStatus(1L, AppointmentStatus.COMPLETED))
                .thenReturn(2L);
        when(appointmentRepository.save(any())).thenReturn(appointment);

        appointmentService.updateStatus(1L, AppointmentStatus.COMPLETED);

        verify(loyaltyService).awardPoints(1L, 15);
        assertThat(client.getLastCompletedAt()).isNotNull();
        assertThat(client.getTier()).isEqualTo(ClientTier.REGULAR); // count=2, +1=3 → REGULAR
    }
}
