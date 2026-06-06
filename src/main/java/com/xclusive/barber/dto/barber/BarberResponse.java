package com.xclusive.barber.dto.barber;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BarberResponse {
    private Long id;
    private String name;
    private String specialty;
    private String phone;
}
