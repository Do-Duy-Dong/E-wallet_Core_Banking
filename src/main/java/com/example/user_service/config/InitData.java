package com.example.user_service.config;

import com.example.user_service.model.Account;
import com.example.user_service.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class InitData {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    public InitData(
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Bean
    CommandLineRunner seedAccount (){
        return args -> {

            // tránh seed lặp lại mỗi lần restart
            if (accountRepository.count() > 0) return;

            String hashPass = passwordEncoder.encode("12345");

            Account admin = Account.builder()
                    .accountNumber("ACC001")
                    .userName("guts")
                    .password(hashPass)
                    .fullName("System Administrator")
                    .email("guts@gmail.com")
                    .address("Hanoi")
                    .balance(1_000_000L)
                    .active(true)
                    .role("ROLE_ADMIN")
                    .build();

            Account user1 = Account.builder()
                    .accountNumber("ACC002")
                    .userName("user1")
                    .password(hashPass)
                    .fullName("Nguyen Van A")
                    .email("user1@gmail.com")
                    .address("Ho Chi Minh")
                    .balance(500_000L)
                    .active(true)
                    .role("ROLE_USER")
                    .build();

            Account user2 = Account.builder()
                    .accountNumber("ACC003")
                    .userName("user2")
                    .password(hashPass)
                    .fullName("Tran Thi B")
                    .email("user2@gmail.com")
                    .address("Da Nang")
                    .balance(200_000L)
                    .active(false)
                    .role("ROLE_USER")
                    .build();

            accountRepository.saveAll(List.of(admin, user1, user2));
        };



    }
}
