package com.example.user_service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class TransactionResponse {
    private String fromAcount;
    private String nameAccount;
    private String message;
    private long amount;
    private LocalDateTime createdAt;
}
