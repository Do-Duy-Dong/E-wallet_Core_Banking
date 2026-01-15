package com.example.user_service.service;

import com.example.user_service.dto.BankingRequest;
import com.example.user_service.dto.GetOtpRequest;
import com.example.user_service.exception.ResourceNotFound;
import com.example.user_service.model.*;
import com.example.user_service.repository.OtpRepository;
import com.example.user_service.repository.TransactionRepository;
import com.example.user_service.utils.GenerateOtp;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionsService {
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final OtpRepository otpRepository;
//    STEP 1: GET OTP - POST[/api/v1/banking/transfer/initiate]
    public String initiateTransfer(GetOtpRequest request,String username) {
        if(otpRepository.findByRequestId(request.getRequestId()).isPresent()){
            throw new ResourceNotFound("Wait for previous OTP to expire");
        }
        Account acctTransfer= accountService.getAccountByUserName(username);
        Account acctReceive= accountService.getAccountById(UUID.fromString(request.getToAccount()));
        if(acctTransfer.getBalance()< request.getAmount()){
            throw new ResourceNotFound("Not enough balance");
        }
        String otp= GenerateOtp.generateOtp();
        Otp otpRecord= Otp.builder()
                .requestId(request.getRequestId())
                .toAccount(request.getToAccount())
                .fromAccount(acctTransfer.getUserName())
                .code(otp)
                .message(request.getMessage())
                .amount(request.getAmount())
                .retryCount(0)
                .expiryTime(LocalDateTime.now().plusMinutes(2))
                .build();
        otpRepository.save(otpRecord);
        log.info("OTP for requestId {} is {}",request.getRequestId(), otp);
        return otpRecord.getId().toString();
//        Email send
    }
    @Transactional
    public void confirmTransaction(BankingRequest request, String username){
        if(transactionRepository.findByRequestId(request.getRequestId()).isPresent()){
            throw new ResourceNotFound("Transaction with this requestId already exists");
        }
        Otp otpRecord= otpRepository.findById(UUID.fromString(request.getOtpId()))
                .orElseThrow(()-> new ResourceNotFound("Transaction not valid"));
        if(otpRecord.getExpiryTime().isBefore(LocalDateTime.now())){
            throw new ResourceNotFound("OTP expired");
        }
        if(otpRecord.getRetryCount()>=3){
            throw new ResourceNotFound("Maximum retry attempts exceeded");
        }
        if(!otpRecord.getCode().equals(request.getOtp())){
            otpRecord.setRetryCount(otpRecord.getRetryCount()+1);
            otpRepository.save(otpRecord);
            throw new ResourceNotFound("Invalid OTP");
        }
//        UPDATE BALANCE
        Account accTransfer= accountService.getAccountByUserName(username);
        Account accReceive= accountService.getAccountById(UUID.fromString(otpRecord.getToAccount()));
        accTransfer.setBalance(accTransfer.getBalance()- otpRecord.getAmount());
        accReceive.setBalance(accReceive.getBalance()+ otpRecord.getAmount());
        accountService.saveAccount(accTransfer);
        accountService.saveAccount(accReceive);
//        SAVE TRANSACTION
        try{
            Transaction transaction = Transaction.builder()
                    .fromAccount(accTransfer)
                    .toAccount(accReceive)
                    .amount(otpRecord.getAmount())
                    .message(otpRecord.getMessage())
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
        otpRepository.delete(otpRecord);
//      SEND EMAIL

    }
}
