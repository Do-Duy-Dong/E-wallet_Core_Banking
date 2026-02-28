package com.example.user_service.repository;

import com.example.user_service.model.Account;
import com.example.user_service.model.LinkedBank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LinkedBankRepository extends JpaRepository<LinkedBank, UUID> {
    Optional<LinkedBank> findByAccount_UserName(String username);
    Optional<LinkedBank> findByBankNo(String bankNo);
    Optional<LinkedBank> findByIdAndAccount(UUID bankId, Account account);
}
