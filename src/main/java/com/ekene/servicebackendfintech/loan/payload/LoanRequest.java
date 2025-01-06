package com.ekene.servicebackendfintech.loan.payload;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoanRequest {
    private String transactionReference;
    private BigDecimal amount;
    private int tenure;
}
