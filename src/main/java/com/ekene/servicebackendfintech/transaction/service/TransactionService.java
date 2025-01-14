package com.ekene.servicebackendfintech.transaction.service;

import com.ekene.servicebackendfintech.loan.payload.LoanResponse;
import com.ekene.servicebackendfintech.transaction.enums.TransactionType;
import com.ekene.servicebackendfintech.transaction.model.Transaction;
import com.ekene.servicebackendfintech.transaction.payload.TransactionRequest;
import com.ekene.servicebackendfintech.transaction.repository.TransactionRepository;
import com.ekene.servicebackendfintech.utils.ApiResponse;
import com.ekene.servicebackendfintech.utils.ValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final ValidationService validationService;

    public void recordTransaction(TransactionRequest request, String userId) {
        boolean ifItsRepaid = request.getType().equals(TransactionType.REPAYMENT);
        validationService.validateTransaction(request, userId);

        Transaction transaction = Transaction.builder()
                .type(request.getType())
                .repaid(ifItsRepaid)
                .transactionDate(LocalDate.now())
                .amount(request.getAmount())
                .userId(userId)
                .transactionReference(request.getTransactionReference()).build();

        if (ifItsRepaid){
            transaction.setDisbursementReference(request.getTransactionReference());
            transaction.setTransactionReference("RPT" + LocalDateTime.now());
        }

        log.info("Recording transaction for loan: {}", request.getTransactionReference());
        transactionRepository.save(transaction);
    }

    public ResponseEntity<ApiResponse<Page<Transaction>>> generateStatement(String userId, LocalDate from, LocalDate to, int size, int length) {
        Pageable pageRequest = PageRequest.of(size, length);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>("Successfully Fetched User statement",
                        LocalDateTime.now(), transactionRepository.findByUserIdAndDateRange(userId, from, to, pageRequest)));
    }
}
