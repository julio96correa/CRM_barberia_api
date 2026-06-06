package com.xclusive.barber.dto.appointment;

import com.xclusive.barber.enums.AppointmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class AppointmentResponse {
    private Long id;
    private String clientName;
    private String barberName;
    private String serviceName;
    private LocalDate appointmentDate;
    private Integer startHour;
    private AppointmentStatus status;
}
