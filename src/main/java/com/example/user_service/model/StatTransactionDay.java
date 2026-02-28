package com.example.user_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "stat_transaction_day")
public class StatTransactionDay extends BaseClass {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private long totalAmount = 0L;
    private long totalReceive=0L;
    private long totalTransfer=0L;
    private long totalDeposit=0L;
    private long totalWithdraw=0L;
    private long totalTransactions=0L;
    private long totalTransferCount=0L;
    private long totalDepositCount=0L;
    private long totalWithdrawCount=0L;
    private UUID fromAccountId;
    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;
}
