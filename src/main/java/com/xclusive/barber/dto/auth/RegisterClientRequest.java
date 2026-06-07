package com.xclusive.barber.dto.auth;

import lombok.Data;

@Data
public class RegisterClientRequest {
    private String name;
    private String email;
    private String password;
    private String phone;
}
