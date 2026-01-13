package com.example.user_service.service;

import com.example.user_service.config.JWT_config.JWTService;
import com.example.user_service.dto.AccountResponse;
import com.example.user_service.dto.LoginRequest;
import com.example.user_service.model.Account;
import com.example.user_service.repository.AccountRepository;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final JWTService jwtService;
    private final PasswordEncoder passwordEncoder;
    public Account getAccountByUserName(String username) {
        return accountRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }
    public Map<String,String> login(LoginRequest request){
        Account acc= getAccountByUserName(request.getUserName());
        passwordEncoder.matches(request.getPassword(), acc.getPassword());
        String AT= jwtService.generateAccessToken(acc.getUserName());
        String RT= jwtService.generateRefreshToken(acc.getUserName());

        return Map.of(
                "accessToken", AT,
                "refreshToken", RT
        );
    }
}
