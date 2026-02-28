package com.example.user_service.dto;

import com.example.user_service.dto.projection.DailyStatProjection;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatDaily {
    private UUID from_account_id;
    private Long deposit_sum;
    private Long withdraw_sum;
    private Long transfer_sum;
    private Long deposit_count;
    private Long withdraw_count;
    private Long transfer_count;

    private StatDaily(DailyStatProjection projection) {
        this.from_account_id = projection.getFromAccountId();
        this.deposit_sum = projection.getDepositSum();
        this.withdraw_sum = projection.getWithdrawSum();
        this.transfer_sum = projection.getTransferSum();
        this.deposit_count = projection.getDepositCount();
        this.withdraw_count = projection.getWithdrawCount();
        this.transfer_count = projection.getTransferCount();
    }
}
