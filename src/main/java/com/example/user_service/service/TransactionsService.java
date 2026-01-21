package com.example.user_service.service;

import com.example.user_service.dto.*;
import com.example.user_service.exception.ResourceNotFound;
import com.example.user_service.model.*;
import com.example.user_service.repository.OtpRepository;
import com.example.user_service.repository.TransactionRepository;
import com.example.user_service.utils.GenerateOtp;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionsService {
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final OtpRepository otpRepository;
    private final EmailService emailService;
    private final RedisTemplate<String,Object> redisTemplate;
//    STEP 1: GET OTP - POST[/api/v1/banking/transfer/initiate]
    public String initiateTransfer(GetOtpRequest request,String username) {
        String key="otp:"+request.getRequestId();
//        impodenticy check with requestId
        if(redisTemplate.hasKey(key)) {
            throw new ResourceNotFound("Wait for previous OTP to expire");
        }
        Account acctTransfer= accountService.getAccountByUserName(username);
        Account acctReceive= accountService.getAccountById(UUID.fromString(request.getToAccount()));
        if(acctTransfer.getBalance()< request.getAmount()){
            throw new ResourceNotFound("Not enough balance");
        }

//        Save to Redis cache
        String otp= GenerateOtp.generateOtp();
        Map<String,Object> map = Map.of(
                "toAccount", request.getToAccount(),
                "fromAccount", acctTransfer.getUserName(),
                "amount", request.getAmount(),
                "message", request.getMessage(),
                "code", otp,
                "retryCount", 0
        );
        redisTemplate.opsForHash().putAll(key, map);
        redisTemplate.expire(key,3,TimeUnit.MINUTES);

        log.info("OTP for requestId {} is {}",request.getRequestId(), otp);

//        Email send Async
        MailVariable mailVariable= MailVariable.builder()
                .name(acctTransfer.getFullName())
                .email(acctReceive.getEmail())
                .otp(otp)
                .subject("Your OTP Code")
                .build();
        emailService.sendEmail(mailVariable);
        return request.getRequestId();
    }

    @Transactional
    public void confirmTransaction(BankingRequest request, String username){
//        Check requestId of transaction
        if(transactionRepository.findByRequestId(request.getRequestId()).isPresent()){
            throw new ResourceNotFound("Transaction with this requestId already exists");
        }
//        Get data from Redis and validate OTP
        String key= "otp:"+request.getOtpId();
        Map<Object,Object> otpRecord= redisTemplate.opsForHash().entries(key);
        Integer retryCount= (Integer) otpRecord.get("retryCount");
        String otpCode = (String) otpRecord.get("code");
        if(otpRecord.isEmpty()){
            throw new ResourceNotFound("OTP expired");
        }
        if(retryCount>=3){
            throw new ResourceNotFound("Maximum retry attempts exceeded");
        }
        if(!otpCode.equals(request.getOtp())){
//            Transaction not retry even main transaction fails
            Long newRetryCount= increaseRetryCount(key);
            if(newRetryCount>=3){
                throw new ResourceNotFound("Maximum retry attempts exceeded");
            }else {
                throw new ResourceNotFound("Invalid OTP");
            }
        }
//        UPDATE BALANCE
        Long amount= ((Number) otpRecord.get("amount")).longValue();
        Account accTransfer= accountService.getAccountByUserName(username);
        Account accReceive= accountService.getAccountById(UUID.fromString((String) otpRecord.get("toAccount")));
        accTransfer.setBalance(accTransfer.getBalance()- amount);
        accReceive.setBalance(accReceive.getBalance()+ amount);
        accountService.saveAccount(accTransfer);
        accountService.saveAccount(accReceive);
//        SAVE TRANSACTION
        try{
            Transaction transaction = Transaction.builder()
                    .fromAccount(accTransfer)
                    .toAccount(accReceive)
                    .amount(amount)
                    .message((String) otpRecord.get("message"))
                    .requestId(request.getRequestId())
                    .type(TypeEnum.TRANSFER)
                    .status(StatusEnum.COMPLETED)
                    .build();
            transactionRepository.save(transaction);
        }catch (Exception e){
            log.error("Error saving transaction: {}", e.getMessage());
            throw new RuntimeException("Error processing transaction");

        }

//        DELETE OTP RECORD
        redisTemplate.delete("otp:"+request.getOtpId());
//      SEND EMAIL

    }


    public ResponsePageBase<TransactionResponse> getTransactions(String username, int page){
        Account account= accountService.getAccountByUserName(username);
        Pageable pageable= PageRequest.of(
                page-1,
                10,
                Sort.by("createdAt").descending());
        Page<Transaction> records= transactionRepository.findByFromAccount_Id(account.getId(),pageable);
        List<TransactionResponse> transactionResponses= records.getContent().stream()
                .map(tr-> TransactionResponse.builder()
                        .fromAcount(tr.getFromAccount().getAccountNumber())
                        .nameAccount(tr.getToAccount().getFullName())
                        .message(tr.getMessage())
                        .amount(tr.getAmount())
                        .createdAt(tr.getCreatedAt())
                        .build()

                ).toList();
        return ResponsePageBase.<TransactionResponse>builder()
                .content(transactionResponses)
                .pageNumber(records.getNumber()+1)
                .pageSize(records.getTotalPages())
                .build();
    }

    @org.springframework.transaction.annotation.Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long increaseRetryCount(String key){
        return redisTemplate.opsForHash().increment(key,"retryCount",1);
    }
}
