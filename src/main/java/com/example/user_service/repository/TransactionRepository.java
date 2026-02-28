package com.example.user_service.repository;

import com.example.user_service.dto.StatDaily;
import com.example.user_service.dto.projection.DailyStatProjection;
import com.example.user_service.model.StatusEnum;
import com.example.user_service.model.Transaction;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByRequestId(String requestId);
    Page<Transaction> findByFromAccount_Id(UUID fromAccount, Pageable pageable);
    Optional<Transaction> findByRequestIdAndStatus(String requestId, StatusEnum status);
    @Query(value = """
            SELECT * 
            FROM transaction
            WHERE status = 'PENDING'
                    AND created_at <= NOW() - INTERVAL '2 minutes'
            LIMIT :limit
            """,nativeQuery = true)
    List<Transaction> findAllByStatus(
            @Param("limit") int limit
    );

    @Query(value = """ 
            SELECT SUM(amount)
            FROM transaction
            WHERE status='COMPLETED'
                AND from_account_id = :id
                AND type='TRANSFER'
                AND updated_at >= :startRange
                AND updated_at <= :endRange
            """, nativeQuery = true)
    Long totalTransferByTime(
            @Param("id") UUID id,
            @Param("startRange") LocalDateTime startRange,
            @Param("endRange") LocalDateTime endRange
    );

    @Query(value = """ 
            SELECT a.from_account_id,
             SUM(CASE WHEN a.type='DEPOSIT' THEN a.amount ELSE 0 END) AS depositSum,
              SUM(CASE WHEN a.type='WITHDRAW' THEN a.amount ELSE 0 END) AS withdrawSum,
              SUM(CASE WHEN a.type='TRANSFER' THEN a.amount ELSE 0 END) AS transferSum,
              COUNT(CASE WHEN a.type='DEPOSIT' THEN a.type END) AS depositCount,
              COUNT(CASE WHEN a.type='WITHDRAW' THEN a.type END) AS withdrawCount,
              COUNT(CASE WHEN a.type='TRANSFER' THEN a.type END) AS transferCount
            FROM public.transaction a
            WHERE updated_at >= :startRange
                AND updated_at <= :endRange
            GROUP BY a.from_account_id
            """, nativeQuery = true)
    List<DailyStatProjection> TransactionByTime(
            @Param("startRange") LocalDateTime startRange,
            @Param("endRange") LocalDateTime endRange
    );
    @Query(value = """
            SELECT SUM(amount)
            FROM public.transaction
            WHERE status='COMPLETED'
            	AND to_account_id = :id
            	AND updated_at >= :startRange
            	AND updated_at <= :endRange
            """,nativeQuery = true)
    Long totalReceiveByTime(
            @Param("id") UUID id,
            @Param("startRange") LocalDateTime startRange,
            @Param("endRange") LocalDateTime endRange
    );



}