package com.example.user_service.dto;

import com.example.user_service.model.TypeEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RequestChangeFund {
    private String requestId;
    private String linkBankNo;
    private String accountNumber;
    private TypeEnum type;
    private long amount;
    private String signature;
}
