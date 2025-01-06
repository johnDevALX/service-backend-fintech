package com.ekene.servicebackendfintech.loan.repository;

import com.ekene.servicebackendfintech.loan.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    @Query("SELECT l FROM Loan l WHERE l.userEmail = :userEmail ORDER BY l.createdAt DESC")
    Loan findMostRecentLoanByUserEmail(@Param("userEmail") String userEmail);
    Loan findByUserEmailEqualsIgnoreCaseAndTransactionReferenceEqualsIgnoreCase(String email, String transactionReference);

    Loan getLoanByTransactionReferenceIgnoreCase(String transactionReference);
}
