package com.example.user_service.service;

import com.example.user_service.config.JobSchedule;
import com.example.user_service.dto.*;
import com.example.user_service.exception.ResourceNotFound;
import com.example.user_service.model.*;
import com.example.user_service.repository.AccountRepository;
import com.example.user_service.repository.LinkedBankRepository;
import com.example.user_service.repository.TransactionRepository;
import com.example.user_service.utils.GenerateOtp;
import com.example.user_service.utils.HmacSignature;
import com.example.user_service.utils.ValidateConfirmTS;
import com.sun.net.httpserver.Headers;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class LinkedBankService {
    @Value("${banking.api.url}")
    private String url;
    @Value("${signature.secret}")
    private String secretKey;
    private final LinkedBankRepository linkedBankRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final JobSchedule jobSchedule;
    private final OtpService otpService;
    private final RestTemplate restTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    public LinkedBank getLinkBankByAccountNumberAndAccount(
            UUID bankNo,
            Account account) {
        return linkedBankRepository.findByIdAndAccount(bankNo, account)
                .orElseThrow(() -> new ResourceNotFound("Linked bank account not found"));
    }

    public void linkBankAccount(
            String userName,
            String bankNo,
            String bankName) {
        Account account = accountRepository.findByUserName(userName)
                .orElseThrow(() -> new ResourceNotFound("Account not found"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> body = Map.of(
                "partnerId", account.getAccountNumber(),
                "bankNo", bankNo);
        try {
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url + "/link",
                    HttpMethod.POST,
                    request,
                    String.class);
            if (response.getStatusCode() == HttpStatus.CREATED) {
                LinkedBank linkedBank = LinkedBank.builder()
                        .bankName(bankName)
                        .bankNo(bankNo)
                        .token(response.getBody().toString())
                        .active(true)
                        .account(account)
                        .build();
                linkedBankRepository.save(linkedBank);
            }
        } catch (Exception e) {
            log.error("Error linking bank account: {}", e);
            throw new ResourceNotFound("Failed to link bank account");
        }
    }

    @Async
    public void sendRequestToBank(OtpRecord otpRecord, String token, String type) {
        HttpHeaders header = new HttpHeaders();
        header.set("X-Partner-Token", token);
        header.set("X-Signature", otpRecord.getSignature());
        header.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "requestId", otpRecord.getRequestId(),
                "bankNo", otpRecord.getToAccount(),
                "walletNo", otpRecord.getFromAccount(),
                "amount", otpRecord.getAmount(),
                "timestamp", System.currentTimeMillis());
        HttpEntity<Map<String, Object>> requestHttp = new HttpEntity<>(body, header);
        try {
            ResponseEntity<BankCallBackResponse> response = restTemplate.exchange(
                    url + "/" + type,
                    HttpMethod.POST,
                    requestHttp,
                    BankCallBackResponse.class);
            callbackMockBank(response.getBody());
        }
        catch(ResourceAccessException e){
            log.error("Bank API is not reachable: {}", e);
//            Job Call Bank API to check processs
           jobSchedule.schedulePendingTs(otpRecord.getRequestId());
           throw new RuntimeException("Bank API is not reachable,we will retry later");
        }
        catch (Exception e) {
            log.error("Error depositing fund to bank: {}", e);
            throw new ResourceNotFound("Failed to deposit fund to bank");
        }
    }

    @Transactional
    public void callbackMockBank(
            BankCallBackResponse request) {
        Transaction transaction = transactionRepository
                .findByRequestIdAndStatus(request.getRequestId(), StatusEnum.PENDING)
                .orElseThrow(() -> new ResourceNotFound("Transaction not found"));

        Account account = accountRepository.findByAccountNumber(request.getWalletNo())
                .orElseThrow(() -> new ResourceNotFound("Account not found"));

        if (request.getStatus() == 1) {
            if (request.getType().equals(TypeEnum.DEPOSIT.name())) {
                // DEPOSIT: Increase balance
                account.setBalance(account.getBalance() + request.getAmount());
                accountRepository.save(account);

            } else if (request.getType().equals(TypeEnum.WITHDRAW.name())) {
                // WITHDRAW: decrease balance and release the hold
                account.setBalance(account.getBalance() - request.getAmount());
                account.setHeld(account.getHeld() - request.getAmount());
                accountRepository.save(account);
            }
            transaction.setStatus(StatusEnum.COMPLETED);
            transactionRepository.save(transaction);
            log.info("Request success for walletNo: {}", request.getWalletNo());
        } else {
            // Bank rejected the transaction — release hold funds (WITHDRAW) and mark FAILED
            if (request.getType() != null && request.getType().equals(TypeEnum.WITHDRAW.name())) {
                account.setHeld(account.getHeld() - request.getAmount());
                accountRepository.save(account);
            }
            transaction.setStatus(StatusEnum.FAILED);
            transactionRepository.save(transaction);
            log.warn("Transaction failed at bank side for requestId: {}", request.getRequestId());
        }
    }

}
