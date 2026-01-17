package com.example.user_service.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OtpRecord {
    private String requestId;
    private String toAccount;
    private String fromAccount;
    private long amount;
    private String message;
    private int retryCount;
    private String code;
}
