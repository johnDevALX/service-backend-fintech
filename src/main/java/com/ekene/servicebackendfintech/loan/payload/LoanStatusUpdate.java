package com.ekene.servicebackendfintech.loan.payload;

import com.ekene.servicebackendfintech.loan.enums.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoanStatusUpdate {
    private Long loanId;
    private LoanStatus status;
}
