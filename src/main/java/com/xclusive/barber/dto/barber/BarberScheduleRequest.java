package com.xclusive.barber.dto.barber;

import lombok.Data;

@Data
public class BarberScheduleRequest {
    private Integer dayOfWeek;
    private Integer startHour;
    private Integer endHour;
    private Integer breakStartHour;
    private Boolean isAvailable;
}
