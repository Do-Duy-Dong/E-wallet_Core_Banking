package com.example.user_service.utils;

import com.example.user_service.dto.VerifyOtp;
import com.example.user_service.dto.OtpRecord;
import com.example.user_service.exception.ResourceNotFound;
import com.example.user_service.model.TypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ValidateConfirmTS {

}
