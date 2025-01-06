package com.ekene.servicebackendfintech.loan.payload;

import com.ekene.servicebackendfintech.loan.enums.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoanResponse {
    private String transactionReference;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private BigDecimal totalRepaymentAmount;
    private BigDecimal outstandingAmount;
    private LoanStatus status;
    private Integer tenure;
}
