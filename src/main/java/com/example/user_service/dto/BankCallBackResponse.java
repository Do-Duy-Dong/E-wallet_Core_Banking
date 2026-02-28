package com.example.user_service.dto;

import com.example.user_service.model.TypeEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BankCallBackResponse {
    private String requestId;
    private int status;
    private String bankNo;
    private String walletNo;
    private Long amount;
    private String type;
}
