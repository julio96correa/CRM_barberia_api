package com.xclusive.barber.dto.barber;

import lombok.Data;

@Data
public class BarberScheduleRequest {
    private String dayOfWeek;    // "MONDAY", "TUESDAY", etc.
    private String startTime;    // "HH:mm"
    private String endTime;      // "HH:mm"
    private Integer breakStartHour;
    private Boolean available;
}
