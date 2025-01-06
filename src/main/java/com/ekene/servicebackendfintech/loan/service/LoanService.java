package com.ekene.servicebackendfintech.loan.service;

import com.ekene.servicebackendfintech.exception.LoanNotFoundException;
import com.ekene.servicebackendfintech.loan.enums.LoanStatus;
import com.ekene.servicebackendfintech.loan.model.Loan;
import com.ekene.servicebackendfintech.loan.payload.LoanRequest;
import com.ekene.servicebackendfintech.loan.repository.LoanRepository;
import com.ekene.servicebackendfintech.transaction.enums.TransactionType;
import com.ekene.servicebackendfintech.transaction.payload.TransactionRequest;
import com.ekene.servicebackendfintech.transaction.service.TransactionService;
import com.ekene.servicebackendfintech.user.model.FintechUser;
import com.ekene.servicebackendfintech.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class LoanService {
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;

    public Loan applyForLoan(LoanRequest request, String userEmail) {
        FintechUser user = getFintechUser(userEmail);

        BigDecimal interestRate = calculateInterestRate(request.getAmount(), request.getTenure());
        BigDecimal totalInterest = calculateTotalInterest(request.getAmount(), interestRate, request.getTenure());
        BigDecimal totalRepayment = request.getAmount().add(totalInterest);

        Loan loan = new Loan();
        loan.setUserId(user.getUserId());
        loan.setTransactionReference(request.getTransactionReference());
        loan.setUserEmail(user.getEmail());
        loan.setUserPhoneNumber(user.getPhoneNumber());
        loan.setPrincipalAmount(request.getAmount());
        loan.setTenure(request.getTenure());
        loan.setInterestRate(interestRate);
        loan.setTotalInterestAmount(totalInterest);
        loan.setTotalRepaymentAmount(totalRepayment);
        loan.setOutstandingAmount(totalRepayment);
        loan.setAmountPaid(BigDecimal.ZERO);
        loan.setRemainingTenure(request.getTenure());
        loan.setExpectedRepaymentDate(LocalDateTime.now().plusMonths(request.getTenure()));

        transactionService.recordTransaction(new TransactionRequest(request.getTransactionReference(), request.getAmount(), TransactionType.DISBURSEMENT), user.getUserId());
        log.info("New loan application: {} for user: {}", loan.getTransactionReference(), user.getEmail());
        return loanRepository.save(loan);
    }

    private BigDecimal calculateTotalInterest(BigDecimal principal, BigDecimal rate, Integer tenure) {
        return principal.multiply(rate)
                .multiply(BigDecimal.valueOf(tenure))
                .divide(BigDecimal.valueOf(1200), 2, RoundingMode.HALF_UP);
    }

    public void updateLoanRepayment(String transactionReference, BigDecimal repaymentAmount, String userEmail) {
        FintechUser user = getFintechUser(userEmail);

        Loan loan = loanRepository.getLoanByTransactionReferenceIgnoreCase(transactionReference);
        transactionService.recordTransaction(new TransactionRequest(transactionReference, repaymentAmount, TransactionType.REPAYMENT),
                user.getUserId());
        loan.setAmountPaid(loan.getAmountPaid().add(repaymentAmount));
        loan.setOutstandingAmount(loan.getTotalRepaymentAmount().subtract(loan.getAmountPaid()));
        loan.setRemainingTenure(calculateRemainingTenure(loan));

        if (loan.getOutstandingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            loan.setStatus(LoanStatus.COMPLETED);
        }

        loanRepository.save(loan);
    }

    private FintechUser getFintechUser(String userEmail) {
        return userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));
    }

    private Integer calculateRemainingTenure(Loan loan) {
        double repaymentProgress = loan.getAmountPaid()
                .divide(loan.getTotalRepaymentAmount(), 4, RoundingMode.HALF_UP)
                .doubleValue();
        return (int) Math.ceil(loan.getTenure() * (1 - repaymentProgress));
    }

    private BigDecimal calculateInterestRate(BigDecimal amount, Integer tenure) {
        BigDecimal baseRate = new BigDecimal("10.0");
        if (amount.compareTo(new BigDecimal("50000")) > 0) {
            baseRate = baseRate.add(new BigDecimal("2.0"));
        }
        if (tenure > 12) {
            baseRate = baseRate.add(new BigDecimal("1.5"));
        }
        return baseRate;
    }

    public Loan getUserLoans(String email, String transactionReference) {
        return loanRepository.findByUserEmailEqualsIgnoreCaseAndTransactionReferenceEqualsIgnoreCase(email, transactionReference);
    }

    public Loan updateLoanStatus(Long loanId, LoanStatus newStatus) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found"));
        loan.setStatus(newStatus);
        log.info("Loan {} status updated to: {}", loanId, newStatus);
        return loanRepository.save(loan);
    }


}

