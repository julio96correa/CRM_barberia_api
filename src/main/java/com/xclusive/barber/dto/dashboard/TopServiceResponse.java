package com.xclusive.barber.dto.dashboard;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopServiceResponse {
    private Long serviceId;
    private String serviceName;
    private long totalAppointments;
    private int rank;
}
