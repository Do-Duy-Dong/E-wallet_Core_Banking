package com.example.user_service.controller;

import com.example.user_service.dto.BankCallBackResponse;
import com.example.user_service.dto.OtpRecord;
import com.example.user_service.dto.RequestChangeFund;
import com.example.user_service.dto.VerifyOtp;
import com.example.user_service.model.TypeEnum;
import com.example.user_service.service.ChangeFundService;
import com.example.user_service.service.LinkedBankService;
import com.example.user_service.utils.HmacSignature;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/linked-bank")
public class LinkBankController {
        @Value("${signature.secret}")
        private String secretKey;
        private final LinkedBankService linkedBankService;
        private final ChangeFundService changeFundService;

        @PostMapping("/link")
        public ResponseEntity<?> linkBankAccount(
                        String bankNo,
                        String bankName,
                        @AuthenticationPrincipal UserDetails userDetails) {
                linkedBankService.linkBankAccount(userDetails.getUsername(), bankNo, bankName);
                return ResponseEntity.ok("Bank account linked successfully");
        }

        @PostMapping("/getOtpDeposit")
        public ResponseEntity<?> getOtpDeposit(
                        @RequestBody RequestChangeFund requestChangeFund,
                        @AuthenticationPrincipal UserDetails userDetails) {
                requestChangeFund.setType(TypeEnum.DEPOSIT);
                String requestId = changeFundService.requestChangeFund(requestChangeFund, userDetails.getUsername());
                return ResponseEntity.ok(Map.of(
                                "message", "OTP sent to your email",
                                "requestId", requestId));
        }

        @PostMapping("/confirmDeposit")
        public ResponseEntity<?> confirmDeposit(
                        @RequestBody VerifyOtp verifyOtp,
                        @AuthenticationPrincipal UserDetails userDetails) {
                changeFundService.sendDepositFundToBank(verifyOtp, userDetails.getUsername());
                return ResponseEntity.ok("Pending");
        }

        @PostMapping("/getOtpWithdraw")
        public ResponseEntity<?> getOtpWithdraw(
                        @RequestBody RequestChangeFund requestChangeFund,
                        @AuthenticationPrincipal UserDetails userDetails) {
                requestChangeFund.setType(TypeEnum.WITHDRAW);
                String requestId = changeFundService.requestChangeFund(requestChangeFund, userDetails.getUsername());
                return ResponseEntity.ok(Map.of(
                                "message", "OTP sent to your email",
                                "requestId", requestId));
        }

        @PostMapping("/confirmWithdraw")
        public ResponseEntity<?> confirmWithdraw(
                        @RequestBody VerifyOtp verifyOtp,
                        @AuthenticationPrincipal UserDetails userDetails) {
                changeFundService.sendWithdrawFundToBank(verifyOtp, userDetails.getUsername());
                return ResponseEntity.ok("Pending");
        }

        @PostMapping("/callback")
        public ResponseEntity<?> callbackFromBank(
                        @RequestBody BankCallBackResponse payload) {
                linkedBankService.callbackMockBank(payload);
                return ResponseEntity.ok("Callback received");
        }

        @PostMapping("/test")
        public ResponseEntity<?> testEndpoint(
                        @RequestParam String requestId,
                        @RequestParam String fromAccount,
                        @RequestParam String toAccount,
                        @RequestParam long amount) {
                String s = HmacSignature.calculateHMAC(
                                requestId, fromAccount, toAccount, amount, secretKey);
                return ResponseEntity.ok(s);
        }

}
