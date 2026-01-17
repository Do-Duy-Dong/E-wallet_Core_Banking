package com.example.user_service.repository;

import com.example.user_service.model.Transaction;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByRequestId(String requestId);
    Page<Transaction> findByFromAccount_Id(UUID fromAccount, Pageable pageable);

    @Query(value = """ 
            SELECT SUM(amount)
            FROM transaction
            WHERE status='COMPLETED'
                AND from_account_id = :id
                AND created_at >= :startRange
                AND created_at <= :endRange
            """, nativeQuery = true)
    Long totalTransferByMonth(
            @Param("id") UUID id,
            @Param("startRange") LocalDateTime startRange,
            @Param("endRange") LocalDateTime endRange
    );
    @Query(value = """
            SELECT SUM(amount)
            FROM public.transaction
            WHERE status='COMPLETED'
            	AND to_account_id = :id
            	AND created_at >= :startRange
            	AND created_at <= :endRange
            """,nativeQuery = true)
    Long totalReceiveByMonth(
            @Param("id") UUID id,
            @Param("startRange") LocalDateTime startRange,
            @Param("endRange") LocalDateTime endRange
    );
}