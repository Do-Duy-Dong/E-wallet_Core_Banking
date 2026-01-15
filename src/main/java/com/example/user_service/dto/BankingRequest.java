package com.example.user_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankingRequest {
    private String requestId;
    private String otpId;
    private String otp;

}
