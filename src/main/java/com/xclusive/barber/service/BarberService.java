package com.xclusive.barber.service;

import com.xclusive.barber.dto.barber.BarberAvailabilityResponse;
import com.xclusive.barber.dto.barber.BarberResponse;
import com.xclusive.barber.dto.barber.BarberScheduleRequest;
import com.xclusive.barber.entity.BarberProfile;
import com.xclusive.barber.entity.BarberSchedule;
import com.xclusive.barber.exception.ResourceNotFoundException;
import com.xclusive.barber.repository.AppointmentRepository;
import com.xclusive.barber.repository.BarberProfileRepository;
import com.xclusive.barber.repository.BarberScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BarberService {

    private final BarberProfileRepository barberProfileRepository;
    private final BarberScheduleRepository barberScheduleRepository;
    private final AppointmentRepository appointmentRepository;

    public List<BarberResponse> getAllBarbers() {
        return barberProfileRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public BarberResponse getBarberById(Long id) {
        BarberProfile barber = barberProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barber not found: " + id));
        return toResponse(barber);
    }

    @Transactional
    public void updateSchedule(Long barberId, List<BarberScheduleRequest> requests) {
        BarberProfile barber = barberProfileRepository.findById(barberId)
                .orElseThrow(() -> new ResourceNotFoundException("Barber not found: " + barberId));

        for (BarberScheduleRequest req : requests) {
            BarberSchedule schedule = barberScheduleRepository
                    .findByBarberProfileIdAndDayOfWeek(barberId, req.getDayOfWeek())
                    .orElse(BarberSchedule.builder().barberProfile(barber).dayOfWeek(req.getDayOfWeek()).build());

            schedule.setStartHour(req.getStartHour());
            schedule.setEndHour(req.getEndHour());
            schedule.setBreakStartHour(req.getBreakStartHour());
            schedule.setIsAvailable(req.getIsAvailable());
            barberScheduleRepository.save(schedule);
        }
    }

    @Transactional
    public void markDayOff(Long barberId, LocalDate date) {
        barberProfileRepository.findById(barberId)
                .orElseThrow(() -> new ResourceNotFoundException("Barber not found: " + barberId));

        int dayOfWeek = date.getDayOfWeek().getValue() % 7;
        BarberSchedule schedule = barberScheduleRepository
                .findByBarberProfileIdAndDayOfWeek(barberId, dayOfWeek)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found for day: " + dayOfWeek));

        schedule.setIsAvailable(false);
        barberScheduleRepository.save(schedule);
    }

    public BarberAvailabilityResponse getAvailableSlots(Long barberId, LocalDate date) {
        barberProfileRepository.findById(barberId)
                .orElseThrow(() -> new ResourceNotFoundException("Barber not found: " + barberId));

        int dayOfWeek = date.getDayOfWeek().getValue() % 7;
        BarberSchedule schedule = barberScheduleRepository
                .findByBarberProfileIdAndDayOfWeek(barberId, dayOfWeek)
                .orElse(null);

        if (schedule == null || !schedule.getIsAvailable()) {
            return BarberAvailabilityResponse.builder()
                    .barberId(barberId)
                    .date(date)
                    .availableSlots(List.of())
                    .build();
        }

        List<Integer> occupied = appointmentRepository.findOccupiedSlots(barberId, date);

        List<Integer> available = new ArrayList<>();
        for (int hour = schedule.getStartHour(); hour < schedule.getEndHour(); hour++) {
            if (!occupied.contains(hour) &&
                (schedule.getBreakStartHour() == null || hour != schedule.getBreakStartHour())) {
                available.add(hour);
            }
        }

        return BarberAvailabilityResponse.builder()
                .barberId(barberId)
                .date(date)
                .availableSlots(available)
                .build();
    }

    private BarberResponse toResponse(BarberProfile barber) {
        return BarberResponse.builder()
                .id(barber.getId())
                .name(barber.getUser().getEmail())
                .specialty(barber.getSpecialty())
                .phone(barber.getPhone())
                .build();
    }
}
