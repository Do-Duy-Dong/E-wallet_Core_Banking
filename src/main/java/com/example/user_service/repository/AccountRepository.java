package com.example.user_service.repository;

import com.example.user_service.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByUserName(String username);
    Optional<Account> findByToken(String token);
    Optional<Account> findByAccountNumber(String accountNumber);

//    List<Account> findAllLimit(UUID userId);
}
