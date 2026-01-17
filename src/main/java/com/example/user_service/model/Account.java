package com.example.user_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
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
//        min value >0
        @Min(0L)
        private Long balance;
        private boolean active;
        private String role;
        @Version
        private long version;

    }
