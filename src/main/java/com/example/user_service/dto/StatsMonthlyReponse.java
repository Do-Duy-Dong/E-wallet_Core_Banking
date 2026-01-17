package com.example.user_service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class StatsMonthlyReponse {
    private Long totalPay;
    private Long totalReceive;
}
