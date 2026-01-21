package com.example.user_service.service;

import com.example.user_service.config.JWT_config.JWTService;
import com.example.user_service.dto.AccountResponse;
import com.example.user_service.dto.LoginRequest;
import com.example.user_service.dto.RegisterRequest;
import com.example.user_service.model.Account;
import com.example.user_service.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

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
    public Account getAccountById(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }
//    API LOGIN [/api/auth/login]
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
//    API REGISTER [/api/auth/register]
    public void register(RegisterRequest request){
        Account acc= Account.builder()
                .userName(request.getUserName())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .address(request.getAddress())
                .balance(0L)
                .active(true)
                .role("USER")
                .build();
        accountRepository.save(acc);
    }
//    API GET INFO [/api/auth/profile]
    public AccountResponse getProfile(String userName){
        Account acc= getAccountByUserName(userName);
        return AccountResponse.builder()
                .userName(acc.getUserName())
                .email(acc.getEmail())
                .fullName(acc.getFullName())
                .role(acc.getRole())
                .address(acc.getAddress())
                .balance(acc.getBalance())
                .build();

    }
    public Account saveAccount(Account account){
        return accountRepository.save(account);
    }
//    API REFRESH TOKEN [/api/auth/refresh]
    public Map<String, String> refreshToken(String refreshToken) {
        String userName = jwtService.extractUsername(refreshToken);
        Account account = getAccountByUserName(userName);
        if (jwtService.isTokenValid(refreshToken, account.getUserName())) {
            String newAccessToken = jwtService.generateAccessToken(account.getUserName());
            return Map.of("accessToken", newAccessToken);
        }
        throw new RuntimeException("Invalid or expired refresh token");
    }
}

