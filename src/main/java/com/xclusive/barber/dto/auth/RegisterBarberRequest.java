package com.xclusive.barber.dto.auth;

import lombok.Data;

@Data
public class RegisterBarberRequest {
    private String name;
    private String email;
    private String password;
    private String phone;
    private String specialty;
}
