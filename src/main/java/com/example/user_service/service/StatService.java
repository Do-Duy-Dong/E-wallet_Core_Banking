package com.example.user_service.service;

import com.example.user_service.dto.DateConfig;
import com.example.user_service.dto.MapStat;
import com.example.user_service.dto.StatDaily;
import com.example.user_service.dto.StatsTimeReponse;
import com.example.user_service.dto.projection.DailyStatProjection;
import com.example.user_service.model.Account;
import com.example.user_service.model.StatTransactionDay;
import com.example.user_service.model.Transaction;
import com.example.user_service.model.TypeEnum;
import com.example.user_service.repository.StatRepository;
import com.example.user_service.repository.TransactionRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.user_service.utils.configDate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatService {
    private static Map<TypeEnum, MapStat> mapTransactionStat=Map.of(
            TypeEnum.TRANSFER, MapStat.builder().build(),
            TypeEnum.DEPOSIT, MapStat.builder().build(),
            TypeEnum.WITHDRAW, MapStat.builder().build()
    );
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final EntityManager entityManager;
    private final StatRepository statRepository;
    public StatsTimeReponse statsTransaction(String username, DateConfig dateRange){
        Account acc= accountService.getAccountByUserName(username);
        Long totalPay = transactionRepository.totalTransferByTime(
                acc.getId(),
                dateRange.getStartRange(),
                dateRange.getEndRange());
        Long totalReceive = transactionRepository.totalReceiveByTime(
                acc.getId(),
                dateRange.getStartRange(),
                dateRange.getEndRange());
        StatsTimeReponse reponse= StatsTimeReponse.builder()
                .totalPay(totalPay !=null? totalPay : 0L)
                .totalReceive(totalReceive !=null? totalReceive : 0L)
                .build();
        return reponse;
    }



    public StatsTimeReponse statPaymentToday(
            String username, LocalDateTime dateTime
    ){
        DateConfig dateRange= configDate.configDay(dateTime);
        return statsTransaction(username, dateRange);
    }
    public StatsTimeReponse statPaymentByMonth(
            String username, LocalDateTime dateTime
    ){
        DateConfig dateRange= configDate.configMonth(dateTime);
        return statsTransaction(username, dateRange);
    }
    @Transactional
    public void statDaily(
            String username, LocalDateTime dateTime
    ){
        DateConfig dateConfig= configDate.configMonth(dateTime);
        List<DailyStatProjection> records= transactionRepository.TransactionByTime(
                dateConfig.getStartRange(),
                dateConfig.getEndRange()
        );
        int batchSize=1000;
        int start= Instant.now().getNano();
        for(int i=0; i< records.size(); i++){
            DailyStatProjection record= records.get(i);
            StatTransactionDay statTransactionDay= StatTransactionDay.builder()
                    .fromAccountId(record.getFromAccountId())
                    .totalTransfer(record.getTransferSum())
                    .totalDeposit(record.getDepositSum())
                    .totalWithdraw(record.getWithdrawSum())
                    .totalTransferCount(record.getTransferCount())
                    .totalDepositCount(record.getDepositCount())
                    .totalWithdrawCount(record.getWithdrawCount())
                    .build();
            entityManager.persist(statTransactionDay);
            if(i>0 && i%batchSize==0){
                entityManager.flush();
                entityManager.clear();
                System.out.println("Inserted batch up to index: " + i);
            }
        }
        entityManager.flush();
        entityManager.clear();
        int end= Instant.now().getNano();
        System.out.println("Total time taken (nanoseconds): " + (end - start));

    }
    public void statDailySaveAll(
            String username, LocalDateTime dateTime
    ){
        DateConfig dateConfig= configDate.configMonth(dateTime);
        List<DailyStatProjection> records= transactionRepository.TransactionByTime(
                dateConfig.getStartRange(),
                dateConfig.getEndRange()
        );
        int batchSize=1000;
        int start= Instant.now().getNano();
        List<StatTransactionDay> list= new ArrayList<>();
        for(int i=0; i< records.size(); i++){
            DailyStatProjection record= records.get(i);
            StatTransactionDay statTransactionDay= new StatTransactionDay();
            statTransactionDay.setFromAccountId(record.getFromAccountId());
            statTransactionDay.setTotalTransfer(record.getTransferSum());
            statTransactionDay.setTotalDeposit(record.getDepositSum());
            statTransactionDay.setTotalWithdraw(record.getWithdrawSum());
            statTransactionDay.setTotalTransferCount(record.getTransferCount());
            statTransactionDay.setTotalDepositCount(record.getDepositCount());
            statTransactionDay.setTotalWithdrawCount(record.getWithdrawCount());

            list.add(statTransactionDay);
            if(i>0 && i%batchSize==0){
                statRepository.saveAll(list);
                System.out.println("Inserted batch up to index: " + i);
            }
        }
        statRepository.saveAll(list);
        int end= Instant.now().getNano();
        System.out.println("Total time taken (nanoseconds): " + (end - start));

    }



}
