package com.example.user_service.service;

import com.example.user_service.dto.OtpRecord;
import com.example.user_service.dto.RequestChangeFund;
import com.example.user_service.dto.VerifyOtp;
import com.example.user_service.exception.ResourceNotFound;
import com.example.user_service.model.TypeEnum;
import com.example.user_service.utils.GenerateOtp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {
    private final Map<Enum,String> receiverType=Map.of(
            TypeEnum.DEPOSIT,"toBankNo",
            TypeEnum.WITHDRAW,"toBankNo",
            TypeEnum.TRANSFER,"fromAccount"
    );

    private final RedisTemplate<String,Object> redisTemplate;

    public String createOtp(
            RequestChangeFund request,
            String signature
    ){
        String otp = GenerateOtp.generateOtp();
        Map<String,Object> data= Map.of(
                "requestId",request.getRequestId(),
                "fromAccount", request.getAccountNumber(),
                "toBankNo", request.getLinkBankNo(),
                "amount", String.valueOf(request.getAmount()),
                "code", otp,
                "type", request.getType().name(),
                "signature", signature,
                "retryCount",0
        );
        redisTemplate.opsForHash().putAll( request.getType().name()+request.getRequestId(),data);
        redisTemplate.expire(request.getType().name() +request.getRequestId(),120, TimeUnit.SECONDS);
        return otp;
    }

    public OtpRecord validateOtpRecord(
            VerifyOtp request,
            String username,
            TypeEnum type
    ){
        Map<Object,Object> record= validateGeneral(request,username,type.name());
        OtpRecord otpRecord= OtpRecord.builder()
                .requestId((String) record.get("requestId"))
                .fromAccount((String) record.get("fromAccount"))
                .toAccount((String) record.get(receiverType.get(type)))
                .signature((String) record.get("signature"))
                .type((TypeEnum) TypeEnum.valueOf((String) record.get("type")))
                .amount( Long.parseLong(String.valueOf(record.get("amount"))))
                .build();
        return otpRecord;
    }

    public Map<Object,Object> validateGeneral(
            VerifyOtp request,
            String username,
            String transactionType
    ){

//        Get data from Redis and validate OTP
        String key= transactionType+request.getRequestId();
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
//        DELETE OTP RECORD
        Boolean checkRedis=redisTemplate.delete(key);
        if(!checkRedis){
            log.error("Failed to delete OTP record for requestId: {}", request.getRequestId());
            throw new ResourceNotFound("Server busy, try again later");
        }
        return otpRecord;
    }


    @org.springframework.transaction.annotation.Transactional(propagation = Propagation.REQUIRES_NEW)
    private  Long increaseRetryCount(String key){
        return redisTemplate.opsForHash().increment(key,"retryCount",1);
    }

}
