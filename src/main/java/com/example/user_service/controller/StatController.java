package com.example.user_service.controller;

//import com.example.user_service.dto.StatsMonthlyReponse;
import com.example.user_service.service.StatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stats")
public class StatController {
    private final StatService statService;

//    @GetMapping("/monthly-pay")
//    public ResponseEntity<StatsMonthlyReponse> getMonthlyStats(
//            @AuthenticationPrincipal UserDetails userDetails
//            ){
//        StatsMonthlyReponse reponse = statService.statsMonthlyPayment(userDetails.getUsername(), LocalDateTime.now());
//        return ResponseEntity.ok(reponse);
//    }
}
