package com.example.user_service.controller;

import com.example.user_service.dto.AccountResponse;
import com.example.user_service.dto.LoginRequest;
import com.example.user_service.dto.RegisterRequest;
import com.example.user_service.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final AccountService accountService;
    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request
    ){
        Map<String,String> tokens= accountService.login(request);
        return ResponseEntity.ok(tokens);
    }
    @GetMapping("/api/account/profile")
    public ResponseEntity<?> getProfile(
            @AuthenticationPrincipal UserDetails userDetails
            ){
        AccountResponse acc= accountService.getProfile(userDetails.getUsername());
        return ResponseEntity.ok(acc);
    }
    @PostMapping("/api/auth/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request
            ){
        accountService.register(request);
        return ResponseEntity.ok("Register successfully");
    }
}
