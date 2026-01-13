package com.example.user_service.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "account")
    public class Account extends BaseClass{
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;
        private String accountNumber;
        private String userName;
        private String password;
        private String fullName;
        private String email;
        private String address;
        private long balance;
        private boolean active;
        private String role;
        @Version
        private long version;

    }
