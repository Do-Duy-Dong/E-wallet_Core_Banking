package com.example.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetOtpRequest {
    private String requestId;
    private String fromAccount;
    private String toAccount;
    private long amount;
    private String message;
    private String signature;
}
