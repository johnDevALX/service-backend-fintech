package com.ekene.servicebackendfintech.loan.service;

import com.ekene.servicebackendfintech.exception.LoanNotFoundException;
import com.ekene.servicebackendfintech.loan.enums.LoanStatus;
import com.ekene.servicebackendfintech.loan.model.Loan;
import com.ekene.servicebackendfintech.loan.payload.LoanRequest;
import com.ekene.servicebackendfintech.loan.payload.LoanResponse;
import com.ekene.servicebackendfintech.loan.repository.LoanRepository;
import com.ekene.servicebackendfintech.transaction.enums.TransactionType;
import com.ekene.servicebackendfintech.transaction.payload.TransactionRequest;
import com.ekene.servicebackendfintech.transaction.service.TransactionService;
import com.ekene.servicebackendfintech.user.model.FintechUser;
import com.ekene.servicebackendfintech.user.repository.UserRepository;
import com.ekene.servicebackendfintech.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class LoanService {
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;
    private static final List<LoanStatus> UNALLOWED_STATUS = List.of(LoanStatus.COMPLETED, LoanStatus.REJECTED);

    public ResponseEntity<ApiResponse<LoanResponse>> applyForLoan(LoanRequest request, String userEmail) {
        FintechUser user = getFintechUser(userEmail);
        Loan checkIfLoanExist = loanRepository.getLoanByTransactionReferenceIgnoreCase(request.getTransactionReference());
        if (checkIfLoanExist != null){
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse<>("Transaction reference already exist!",
                            LocalDateTime.now(), null));
        }


        Loan mostRecentLoanByUserEmail = loanRepository.findMostRecentLoanByUserEmail(userEmail);
        if (mostRecentLoanByUserEmail != null && !UNALLOWED_STATUS.contains(mostRecentLoanByUserEmail.getProgressStatus())) {            Loan rejectedLoan = Loan.builder()
                    .userEmail(userEmail)
                    .principalAmount(request.getAmount())
                    .transactionReference(request.getTransactionReference())
                    .tenure(request.getTenure())
                    .progressStatus(LoanStatus.REJECTED)
                    .status(LoanStatus.REJECTED)
                    .build();
            loanRepository.save(rejectedLoan);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse<>("User has an outstanding loan",
                            LocalDateTime.now(), null));
        }

        BigDecimal interestRate = calculateInterestRate(request.getAmount(), request.getTenure());
        BigDecimal totalInterest = calculateTotalInterest(request.getAmount(), interestRate, request.getTenure());
        BigDecimal totalRepayment = request.getAmount().add(totalInterest);

        Loan loan = buildLoan(request, user, interestRate, totalInterest, totalRepayment);
        transactionService.recordTransaction(new TransactionRequest(request.getTransactionReference(), request.getAmount(), TransactionType.DISBURSEMENT), user.getUserId());

        log.info("New loan application: {} for user: {}", loan.getTransactionReference(), user.getEmail());
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>("Successfully applied for loan",
                        LocalDateTime.now(), mapToLoanResponse(loanRepository.save(loan))));
    }

    public ResponseEntity<ApiResponse<LoanResponse>> updateLoanRepayment(String transactionReference, BigDecimal repaymentAmount, String userEmail) {
        FintechUser user = getFintechUser(userEmail);
        Loan loan = loanRepository.getLoanByTransactionReferenceIgnoreCase(transactionReference);
        if (Objects.isNull(loan) || loan.getStatus().equals(LoanStatus.REJECTED)){
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse<>("User can not repay Rejected loan",
                            LocalDateTime.now(), null));
        }

        transactionService.recordTransaction(new TransactionRequest(transactionReference, repaymentAmount, TransactionType.REPAYMENT),
                user.getUserId());
        loan.setAmountPaid(loan.getAmountPaid().add(repaymentAmount));
        loan.setOutstandingAmount(loan.getTotalRepaymentAmount().subtract(loan.getAmountPaid()));
        loan.setRemainingTenure(calculateRemainingTenure(loan));

        if (loan.getOutstandingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            log.info("Successfully made complete repayment of current loan");
            loan.setStatus(LoanStatus.COMPLETED);
        }

        log.info("Successfully made part repayment of current loan");
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>("Successfully repayment operation",
                        LocalDateTime.now(), mapToLoanResponse(loanRepository.save(loan))));
    }

    public ResponseEntity<ApiResponse<LoanResponse>> getUserLoans(String email, String transactionReference) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>("Successfully Fetched loan details",
                        LocalDateTime.now(), mapToLoanResponse(loanRepository.
                findByUserEmailEqualsIgnoreCaseAndTransactionReferenceEqualsIgnoreCase(email, transactionReference))));
    }

    private FintechUser getFintechUser(String userEmail) {
        return userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));
    }

    private Integer calculateRemainingTenure(Loan loan) {
        if (loan.getTenure() == null) {
            log.error("Loan tenure is null for loan with reference: {}", loan.getTransactionReference());
            return 0;
        }

        if (loan.getAmountPaid() == null || loan.getTotalRepaymentAmount() == null) {
            log.error("Amount paid or total repayment amount is null for loan: {}", loan.getTransactionReference());
            return loan.getTenure();
        }

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

    private BigDecimal calculateTotalInterest(BigDecimal principal, BigDecimal rate, Integer tenure) {
        return principal.multiply(rate)
                .multiply(BigDecimal.valueOf(tenure))
                .divide(BigDecimal.valueOf(1200), 2, RoundingMode.HALF_UP);
    }

    public Loan updateLoanStatus(Long loanId, LoanStatus newStatus) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found"));
        loan.setStatus(newStatus);
        log.info("Loan {} status updated to: {}", loanId, newStatus);
        return loanRepository.save(loan);
    }

    private LoanResponse mapToLoanResponse(Loan loan) {
        LoanResponse response = new LoanResponse();
        response.setTransactionReference(loan.getTransactionReference());
        response.setPrincipalAmount(loan.getPrincipalAmount());
        response.setInterestRate(loan.getInterestRate());
        response.setTotalRepaymentAmount(loan.getTotalRepaymentAmount());
        response.setOutstandingAmount(loan.getOutstandingAmount());
        response.setStatus(loan.getStatus());
        response.setTenure(loan.getTenure());
        return response;
    }

    private Loan buildLoan(LoanRequest request, FintechUser user, BigDecimal interestRate, BigDecimal totalInterest, BigDecimal totalRepayment) {
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
        loan.setStatus(LoanStatus.APPROVED);
        loan.setAmountPaid(BigDecimal.ZERO);
        loan.setRemainingTenure(request.getTenure());
        loan.setExpectedRepaymentDate(LocalDateTime.now().plusMonths(request.getTenure()));
        return loan;
    }
}

