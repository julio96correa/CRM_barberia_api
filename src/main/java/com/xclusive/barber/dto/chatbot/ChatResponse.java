package com.xclusive.barber.dto.chatbot;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatResponse {
    private String aiResponse;
    private AppointmentSuggestion appointmentSuggestion;
}
