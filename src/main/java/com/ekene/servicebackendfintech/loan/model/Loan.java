package com.ekene.servicebackendfintech.loan.model;

import com.ekene.servicebackendfintech.loan.enums.LoanStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loans")
@Data
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String userEmail;
    private String userPhoneNumber;
    private String userId;
    private String transactionReference;

    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private Integer tenure;

    // New fields
    private BigDecimal totalInterestAmount;
    private BigDecimal totalRepaymentAmount;
    private LocalDateTime expectedRepaymentDate;
    private BigDecimal outstandingAmount;
    private BigDecimal amountPaid;
    private Integer remainingTenure;

    @Enumerated(EnumType.STRING)
    private LoanStatus status = LoanStatus.PENDING;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
