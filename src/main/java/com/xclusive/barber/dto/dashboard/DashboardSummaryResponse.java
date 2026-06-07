package com.xclusive.barber.dto.dashboard;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class DashboardSummaryResponse {
    private long totalClients;
    private long appointmentsToday;
    private long newClientsThisMonth;
    private Map<String, Long> tierDistribution;
}
