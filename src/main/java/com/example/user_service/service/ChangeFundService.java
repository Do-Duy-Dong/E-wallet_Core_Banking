package com.example.user_service.service;

import com.example.user_service.dto.*;
import com.example.user_service.exception.ResourceNotFound;
import com.example.user_service.model.*;
import com.example.user_service.repository.AccountRepository;
import com.example.user_service.repository.LinkedBankRepository;
import com.example.user_service.repository.TransactionRepository;
import com.example.user_service.utils.Rules;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChangeFundService {

        private final LinkedBankService linkedBankService;
        private final AccountRepository accountRepository;
        private final TransactionRepository transactionRepository;
        private final OtpService otpService;
        private final RedisTemplate<String, Object> redisTemplate;
        private final LinkedBankRepository linkedBankRepository;
        private final Rules rules;

        @Transactional
        public String requestChangeFund(
                        RequestChangeFund request,
                        String userName) {
                Account acc = accountRepository.findByUserName(userName)
                                .orElseThrow(() -> new RuntimeException("Account not found"));

                // 1.Validate rules( WITHDRAW - DEPOSIT)
                ContextInput input = ContextInput.builder()
                                .account(acc)
                                .requestChangeFund(request)
                                .ruleEnum(validRuleEnum(request.getType()))
                                .build();
                rules.validate(input);

                // 2.Create OTP
                String otp = otpService.createOtp(request, request.getSignature());

                ResponseOtpDepost responseOtpDepost = ResponseOtpDepost.builder()
                                .otp(otp)
                                .requestId(request.getRequestId())
                                .build();
                log.info("Otp: {}", responseOtpDepost.getOtp());
                return responseOtpDepost.getRequestId();
        }

        private RuleEnum validRuleEnum(TypeEnum type) {
                if (type == TypeEnum.DEPOSIT)
                        return RuleEnum.DEPOSIT;
                if (type == TypeEnum.WITHDRAW)
                        return RuleEnum.WITHDRAW;
                if (type == TypeEnum.TRANSFER)
                        return RuleEnum.TRANSFER;
                throw new ResourceNotFound("Invalid type");
        }

        public void sendDepositFundToBank(VerifyOtp request, String userName) {
                transactionWithBank(request, userName, TypeEnum.DEPOSIT);
        }

        public void sendWithdrawFundToBank(VerifyOtp request, String userName) {
                transactionWithBank(request, userName, TypeEnum.WITHDRAW);
        }

        @Transactional
        public void transactionWithBank(VerifyOtp request, String userName, TypeEnum type) {

                Account account = accountRepository.findByUserName(userName)
                                .orElseThrow(() -> new ResourceNotFound("Account not found"));

                ContextInput contextInput = ContextInput.builder()
                                .account(account)
                                .requestChangeFund(RequestChangeFund.builder()
                                                .requestId(request.getRequestId())
                                                .build())
                                .ruleEnum(RuleEnum.IDEMPOTENCY)
                                .build();
                // 1. Idempotency check
                rules.validate(contextInput);

                // 2. Validate OTP (uses correct Redis key: type + requestId)
                OtpRecord otpRecord = otpService.validateOtpRecord(request, userName, type);

                LinkedBank linkedBank = linkedBankRepository.findByBankNo(otpRecord.getToAccount())
                                .orElseThrow(() -> new ResourceNotFound("Linked bank account not found"));

                // 3. For WITHDRAW
                if (type == TypeEnum.WITHDRAW) {
                        // 3.1. Balance check
                        contextInput.getRequestChangeFund().setAmount(otpRecord.getAmount());
                        contextInput.setRuleEnum(RuleEnum.BALANCE);
                        rules.validate(contextInput);
                        // 3.2 Hold funds
                        account.setHeld(account.getHeld() + otpRecord.getAmount());
                        accountRepository.save(account);
                }

                // 4. Create transaction with PENDING status
                Transaction transaction = Transaction.builder()
                                .requestId(otpRecord.getRequestId())
                                .fromAccount(account)
                                .linkedBank(linkedBank)
                                .amount(otpRecord.getAmount())
                                .status(StatusEnum.PENDING)
                                .type(type)
                                .build();
                transactionRepository.save(transaction);

                // 5. Send request to bank
                linkedBankService.sendRequestToBank(otpRecord, linkedBank.getToken(), type.name().toLowerCase());
        }
}
