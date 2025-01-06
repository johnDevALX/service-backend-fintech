package com.ekene.servicebackendfintech.loan.repository;

import com.ekene.servicebackendfintech.loan.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    Loan findByUserEmailEqualsIgnoreCaseAndTransactionReferenceEqualsIgnoreCase(String email, String transactionReference);

    Loan getLoanByTransactionReferenceIgnoreCase(String transactionReference);
}
