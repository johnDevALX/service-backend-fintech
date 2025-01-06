package com.ekene.servicebackendfintech.transaction.model;

import com.ekene.servicebackendfintech.transaction.enums.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transactions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_transaction_ref_user_type",
                        columnNames = {"transactionReference", "userId", "type"}
                )
        }
)@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String userId;

    @Column(unique = true)
    private String transactionReference;
    private String disbursementReference;

    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private TransactionType type;
    private boolean repaid;
    private LocalDate transactionDate;
}
