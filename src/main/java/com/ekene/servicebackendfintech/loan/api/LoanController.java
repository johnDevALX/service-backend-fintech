package com.ekene.servicebackendfintech.loan.api;

import com.ekene.servicebackendfintech.auth.CustomUser;
import com.ekene.servicebackendfintech.loan.model.Loan;
import com.ekene.servicebackendfintech.loan.payload.LoanRequest;
import com.ekene.servicebackendfintech.loan.payload.LoanResponse;
import com.ekene.servicebackendfintech.loan.service.LoanService;
import com.ekene.servicebackendfintech.utils.ApiResponse;
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
    public ResponseEntity<ApiResponse<LoanResponse>> applyForLoan(@AuthenticationPrincipal CustomUser user, @Valid @RequestBody LoanRequest request) {
        return loanService.applyForLoan(request, user.getUsername());
    }

    @PutMapping("repay")
    public ResponseEntity<ApiResponse<LoanResponse>> repayLoan(@RequestParam String transactionReference, @RequestParam BigDecimal repaymentAmount,
                                    @AuthenticationPrincipal CustomUser user){
        return loanService.updateLoanRepayment(transactionReference, repaymentAmount, user.getUsername());
    }

    @GetMapping("/get-loan")
    public ResponseEntity<ApiResponse<LoanResponse>> getUserLoans(@AuthenticationPrincipal CustomUser user, @RequestParam String transactionReference) {
        return loanService.getUserLoans(user.getUsername(), transactionReference);
    }
}
