package com.xclusive.barber.dto.dashboard;

import com.xclusive.barber.dto.appointment.AppointmentResponse;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class TodayResponse {
    private LocalDate date;
    private List<AppointmentResponse> appointments;
}
