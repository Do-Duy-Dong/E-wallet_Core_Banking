package com.example.user_service.dto.projection;

import java.math.BigDecimal;
import java.util.UUID;

public interface DailyStatProjection {
    UUID getFromAccountId();
    Long getDepositSum();
    Long getWithdrawSum();
    Long getTransferSum();
    Long getDepositCount();
    Long getWithdrawCount();
    Long getTransferCount();
}
