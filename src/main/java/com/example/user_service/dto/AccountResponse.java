package com.example.user_service.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountResponse {
    private String userName;
    private String email;
    private String fullName;
    private String role;
    private String address;
    private Long balance;

}
