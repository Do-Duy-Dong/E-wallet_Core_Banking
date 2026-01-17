package com.example.user_service.service;

import com.example.user_service.dto.StatsMonthlyReponse;
import com.example.user_service.model.Account;
import com.example.user_service.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatService {
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    public StatsMonthlyReponse statsMonthlyPayment(String username, LocalDateTime dateTime){
        Account acc= accountService.getAccountByUserName(username);
        Map<String, LocalDateTime> dateRange= configDateTime(dateTime);
        Long totalPay = transactionRepository.totalTransferByMonth(
                acc.getId(),
                dateRange.get("startRange"),
                dateRange.get("endRange"));
        Long totalReceive = transactionRepository.totalReceiveByMonth(
                acc.getId(),
                dateRange.get("startRange"),
                dateRange.get("endRange"));

        StatsMonthlyReponse reponse= StatsMonthlyReponse.builder()
                .totalPay(totalPay !=null? totalPay : 0L)
                .totalReceive(totalReceive !=null? totalReceive : 0L)
                .build();
        return reponse;
    }
    
    public Map<String,LocalDateTime>configDateTime(LocalDateTime dateTime){
        LocalDateTime startRange= dateTime.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endRange= startRange.plusMonths(1);
        return Map.of(
                "startRange", startRange,
                "endRange", endRange
        );
    }


}
