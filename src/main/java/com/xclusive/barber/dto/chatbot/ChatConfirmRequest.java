package com.xclusive.barber.dto.chatbot;

import java.time.LocalDate;

public record ChatConfirmRequest(
        Long barberId,
        Long serviceId,
        LocalDate appointmentDate,
        Integer startHour,
        String clientName,
        String clientPhone
) {}
