package com.example.user_service.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Username is not blank")
    private String userName;

    @NotBlank(message = "Password is not blank")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[@$!%*?&])(?=.*[\\d])[a-zA-z@$!%*?&\\d]{8,}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
    )
    private String password;
    @NotBlank(message = "Full name is not blank")
    private String fullName;
    @Email(message = "Email is not valid")
    private String email;
    @NotBlank(message = "Address is not blank")
    private String address;
}
