package com.example.user_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "linked_banks")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkedBank {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String token;
    private String bankName;
    private String bankNo;
    private boolean active=true;
    @OneToOne
    @JoinColumn(name = "account_id")
    private Account account;
}
