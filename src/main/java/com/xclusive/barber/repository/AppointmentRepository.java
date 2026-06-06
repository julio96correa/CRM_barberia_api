package com.xclusive.barber.repository;

import com.xclusive.barber.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("SELECT a.startHour FROM Appointment a WHERE a.barberProfile.id = :barberId AND a.appointmentDate = :date AND a.status NOT IN ('CANCELLED', 'NO_SHOW')")
    List<Integer> findOccupiedSlots(@Param("barberId") Long barberId, @Param("date") LocalDate date);
}
