package com.example.user_service.repository;

import com.example.user_service.model.Otp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OtpRepository extends JpaRepository<Otp, UUID> {
    Optional<Otp> findByRequestId(String requestId);
    Optional<Otp> findByIdAndCode(UUID id, String code);
}
