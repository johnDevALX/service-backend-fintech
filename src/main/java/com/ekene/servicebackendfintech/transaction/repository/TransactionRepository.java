package com.ekene.servicebackendfintech.transaction.repository;

import com.ekene.servicebackendfintech.transaction.enums.TransactionType;
import com.ekene.servicebackendfintech.transaction.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, PagingAndSortingRepository<Transaction, Long> {
    boolean existsByTransactionReferenceIgnoreCase(String transactionReference);

    @Query("SELECT COUNT(t) > 0 FROM Transaction t WHERE t.transactionReference = :reference AND t.userId = :userId AND t.type = :type")
    boolean existsByTransactionReferenceAndUserIdAndType(
            @Param("reference") String reference,
            @Param("userId") String userId,
            @Param("type") TransactionType type
    );

    @Query("SELECT t FROM Transaction t WHERE t.transactionReference = :reference AND t.userId = :userId")
    List<Transaction> findByTransactionReferenceAndUserId(
            @Param("reference") String reference,
            @Param("userId") String userId
    );

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.userId = :userId AND t.type = :type")
    BigDecimal sumAmountByUserIdAndType(
            @Param("userId") String userId,
            @Param("type") TransactionType type
    );

    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId AND t.transactionDate BETWEEN :from AND :to")
    Page<Transaction> findByUserIdAndDateRange(
            @Param("userId") String userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            Pageable pageable
    );}
