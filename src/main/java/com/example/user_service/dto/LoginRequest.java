package com.example.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @NotBlank(message = "Username is not blank")
    private String userName;
    @NotBlank(message = "Password is not blank")
    private String password;
}
