package com.ekene.servicebackendfintech.loan.api;

import com.ekene.servicebackendfintech.auth.CustomUser;
import com.ekene.servicebackendfintech.loan.model.Loan;
import com.ekene.servicebackendfintech.loan.payload.LoanRequest;
import com.ekene.servicebackendfintech.loan.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/loans/")
@RequiredArgsConstructor
@Slf4j
public class LoanController {
    private final LoanService loanService;

    @PostMapping("apply")
//    @RateLimiter(name = "loanApplication")
    public ResponseEntity<Loan> applyForLoan(@AuthenticationPrincipal CustomUser user, @Valid @RequestBody LoanRequest request) {
        return ResponseEntity.ok(loanService.applyForLoan(request, user.getUsername()));
    }

    @PutMapping("repay")
    public ResponseEntity<?> repayLoan(@RequestParam String transactionReference, @RequestParam BigDecimal repaymentAmount,
                                    @AuthenticationPrincipal CustomUser user){
        loanService.updateLoanRepayment(transactionReference, repaymentAmount, user.getUsername());
        return ResponseEntity.ok("Repaid");

    }

    @GetMapping("/get-loan")
    public ResponseEntity<Loan> getUserLoans(@AuthenticationPrincipal CustomUser user, @RequestParam String transactionReference) {
        return ResponseEntity.ok(loanService.getUserLoans(user.getUsername(), transactionReference));
    }
}
