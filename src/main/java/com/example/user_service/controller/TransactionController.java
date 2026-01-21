package com.example.user_service.controller;

import com.example.user_service.dto.BankingRequest;
import com.example.user_service.dto.GetOtpRequest;
import com.example.user_service.dto.ResponsePageBase;
import com.example.user_service.dto.TransactionResponse;
import com.example.user_service.service.TransactionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/baking")
public class TransactionController {
    private final TransactionsService transactionsService;

    @PostMapping("/create-transaction")
    public ResponseEntity<?> createTransaction(
            @RequestBody GetOtpRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            Authentication authentication
    ){
        String otpId=transactionsService.initiateTransfer(request, userDetails.getUsername());

        return ResponseEntity.ok(Map.of(
                "otpRequestId", otpId,
                "message", "OTP sent to your email"
        ));
    }
    @PostMapping("/confirm-transaction")
    public ResponseEntity<?> confirmTransaction(
            @RequestBody BankingRequest request,
            @AuthenticationPrincipal UserDetails userDetails
            ){
        transactionsService.confirmTransaction(request, userDetails.getUsername());
        return ResponseEntity.ok("Transaction successful");
    }

    @GetMapping("/history")
    public ResponseEntity<?> getTransactionHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "page", defaultValue = "1") int page
    ){
        ResponsePageBase<TransactionResponse> res = transactionsService.getTransactions(userDetails.getUsername(), page);
        return ResponseEntity.ok(res);
    }
}
