package com.xclusive.barber.dto.chatbot;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class AppointmentSuggestion {
    private Long barberId;
    private String barberName;
    private LocalDate date;
    private Integer startHour;
    private Long serviceId;
    private String serviceName;
    private String clientName;
    private String clientPhone;
}
