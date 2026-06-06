package com.xclusive.barber.repository;

import com.xclusive.barber.entity.BarberSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BarberScheduleRepository extends JpaRepository<BarberSchedule, Long> {
    List<BarberSchedule> findByBarberProfileId(Long barberId);
    Optional<BarberSchedule> findByBarberProfileIdAndDayOfWeek(Long barberId, int dayOfWeek);
}
