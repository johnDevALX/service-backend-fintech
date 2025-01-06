package com.ekene.servicebackendfintech.utils;

import com.ekene.servicebackendfintech.exception.InvalidTransactionException;
import com.ekene.servicebackendfintech.loan.enums.LoanStatus;
import com.ekene.servicebackendfintech.loan.model.Loan;
import com.ekene.servicebackendfintech.loan.repository.LoanRepository;
import com.ekene.servicebackendfintech.transaction.enums.TransactionType;
import com.ekene.servicebackendfintech.transaction.payload.TransactionRequest;
import com.ekene.servicebackendfintech.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ValidationService {
    private final LoanRepository loanRepository;
    private final TransactionRepository transactionRepository;

    private void validateDisbursement(TransactionRequest transactionRequest) {
        boolean alreadyDisbursed = transactionRepository.existsByTransactionReferenceIgnoreCase(transactionRequest.getTransactionReference());
        if (alreadyDisbursed) {
            throw new InvalidTransactionException("Loan already disbursed");
        }
    }


    public void validateTransaction(TransactionRequest request, String userId) {
        Loan loan = loanRepository.getLoanByTransactionReferenceIgnoreCase(request.getTransactionReference());

        if (request.getType() == TransactionType.DISBURSEMENT) {
            validateDisbursement(request);
        } else {
            validateRepayment(request, loan);
        }
    }

    private void validateRepayment(TransactionRequest request, Loan loan) {
        if (loan.getOutstandingAmount().compareTo(request.getAmount()) < 0) {
            throw new InvalidTransactionException("Repayment amount exceeds outstanding balance");
        }

        if (loan.getStatus() == LoanStatus.COMPLETED) {
            throw new InvalidTransactionException("Loan is already fully repaid");
        }

        boolean repaymentExists = transactionRepository
                .existsByTransactionReferenceAndUserIdAndType(
                        request.getTransactionReference(),
                        loan.getUserEmail(),
                        TransactionType.REPAYMENT
                );

        if (repaymentExists) {
            throw new InvalidTransactionException("Repayment already processed for this reference");
        }
    }
}
