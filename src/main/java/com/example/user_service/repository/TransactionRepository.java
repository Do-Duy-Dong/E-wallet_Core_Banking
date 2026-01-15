package com.example.user_service.repository;

import com.example.user_service.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByRequestId(String requestId);

}
