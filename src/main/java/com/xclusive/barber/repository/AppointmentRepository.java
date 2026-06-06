package com.xclusive.barber.repository;

import com.xclusive.barber.entity.Appointment;
import com.xclusive.barber.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("SELECT a.startHour FROM Appointment a WHERE a.barberProfile.id = :barberId AND a.appointmentDate = :date AND a.status NOT IN ('CANCELLED', 'NO_SHOW')")
    List<Integer> findOccupiedSlots(@Param("barberId") Long barberId, @Param("date") LocalDate date);

    List<Appointment> findByBarberProfileIdAndAppointmentDate(Long barberProfileId, LocalDate appointmentDate);

    Page<Appointment> findByClientProfileIdAndStatus(Long clientProfileId, AppointmentStatus status, Pageable pageable);

    List<Appointment> findByAppointmentDate(LocalDate appointmentDate);

    List<Appointment> findByAppointmentDateBetween(LocalDate start, LocalDate end);

    boolean existsByBarberProfileIdAndAppointmentDateAndStartHour(Long barberProfileId, LocalDate appointmentDate, Integer startHour);

    long countByClientProfileIdAndStatus(Long clientProfileId, AppointmentStatus status);
}
