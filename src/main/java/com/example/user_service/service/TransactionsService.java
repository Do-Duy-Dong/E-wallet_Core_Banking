package com.example.user_service.service;

import com.example.user_service.dto.*;
import com.example.user_service.exception.ResourceNotFound;
import com.example.user_service.model.*;
import com.example.user_service.repository.OtpRepository;
import com.example.user_service.repository.TransactionRepository;
import com.example.user_service.utils.GenerateOtp;
import com.example.user_service.utils.HmacSignature;
import com.example.user_service.utils.ValidateConfirmTS;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionsService {
    @Value("${signature.secret}")
    private String secretKey;
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final OtpRepository otpRepository;
    private final EmailService emailService;
    private final OtpService otpService;
    private final RedisTemplate<String,Object> redisTemplate;
//    STEP 1: GET OTP - POST[/api/v1/banking/transfer/initiate]
    public String initiateTransfer(GetOtpRequest request,String username) {
        String key="otp:"+request.getRequestId();
//        impodenticy check with requestId
        if(redisTemplate.hasKey(key)) {
            throw new ResourceNotFound("Wait for previous OTP to expire");
        }
        String signature= HmacSignature.calculateHMAC(
                request.getRequestId(),
                request.getFromAccount(),
                request.getToAccount(),
                request.getAmount(),
                secretKey
        );
        if(!signature.equals(request.getSignature())){
            throw new ResourceNotFound("Invalid signature");
        }

        Account acctTransfer= accountService.getAccountByUserName(username);
        if(acctTransfer.getBalance()< request.getAmount()){
            throw new ResourceNotFound("Not enough balance");
        }
        Account acctReceive= accountService.getAccountById(UUID.fromString(request.getToAccount()));

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
    @Retryable(
            retryFor = {ObjectOptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public void confirmTransaction(VerifyOtp request, String username){
        //        Check requestId of transaction
        if(transactionRepository.findByRequestId(request.getRequestId()).isPresent()){
            throw new ResourceNotFound("Transaction with this requestId already exists");
        }
//        VALIDATE OTP
        OtpRecord otpRecord= otpService.validateOtpRecord(request,username, TypeEnum.TRANSFER);
//        UPDATE BALANCE
        Long amount= otpRecord.getAmount();
        Account accTransfer= accountService.getAccountByUserName(username);
        Account accReceive= accountService.getAccountById(UUID.fromString(otpRecord.getToAccount()));
        if (accTransfer.getBalance() < amount) {
            throw new ResourceNotFound("Insufficient balance");
        }
        accTransfer.setBalance(accTransfer.getBalance()- amount);
        accReceive.setBalance(accReceive.getBalance()+ amount);
        accountService.saveAccount(accTransfer);
        accountService.saveAccount(accReceive);
//        SAVE TRANSACTION
        try{
            Transaction transaction = Transaction.builder()
                    .fromAccount(accTransfer)
                    .amount(amount)
                    .toAccount(accReceive)
                    .message((String) otpRecord.getMessage())
                    .requestId(request.getRequestId())
                    .type(TypeEnum.TRANSFER)
                    .status(StatusEnum.COMPLETED)
                    .build();
            transactionRepository.save(transaction);
        }catch (Exception e){
            log.error("Error saving transaction: {}", e.getMessage());
            throw new RuntimeException("Error processing transaction");

        }
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


    @Recover
    public void recover(ObjectOptimisticLockingFailureException e){
        log.error("Max retry");
        throw new ResourceNotFound("Server busy, try again later");
    }
}
