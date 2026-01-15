package com.example.user_service.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "otp")
public class Otp extends BaseClass{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(unique = true)
    private String requestId;
    private String toAccount;
    private String fromAccount;
    private long amount;
    private String message;
    private int retryCount;
    private String code;
    private LocalDateTime expiryTime;
}
