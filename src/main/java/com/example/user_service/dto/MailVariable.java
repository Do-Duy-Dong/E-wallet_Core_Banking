package com.example.user_service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MailVariable {
    private String name;
    private String subject;
    private String email;
    private String otp;
}
