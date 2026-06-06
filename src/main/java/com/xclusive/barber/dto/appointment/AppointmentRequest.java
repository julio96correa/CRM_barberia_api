package com.xclusive.barber.dto.appointment;

import java.time.LocalDate;

public record AppointmentRequest(
        Long clientId,
        Long barberId,
        Long serviceId,
        LocalDate appointmentDate,
        Integer startHour
) {}
