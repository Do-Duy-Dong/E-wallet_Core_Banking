package com.example.user_service.repository;

import com.example.user_service.model.StatTransactionDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StatRepository extends JpaRepository<StatTransactionDay, UUID> {
}
