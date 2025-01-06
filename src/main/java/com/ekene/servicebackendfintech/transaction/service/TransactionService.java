package com.ekene.servicebackendfintech.transaction.service;

import com.ekene.servicebackendfintech.transaction.enums.TransactionType;
import com.ekene.servicebackendfintech.transaction.model.Transaction;
import com.ekene.servicebackendfintech.transaction.payload.TransactionRequest;
import com.ekene.servicebackendfintech.transaction.repository.TransactionRepository;
import com.ekene.servicebackendfintech.utils.ValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

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

        log.info("Recording transaction for loan: {}", request.getTransactionReference());
        transactionRepository.save(transaction);
    }

    public Page<Transaction> generateStatement(String userId, LocalDate from, LocalDate to, int size, int length) {
        Pageable pageRequest = PageRequest.of(size, length);
        return transactionRepository.findByUserIdAndDateRange(userId, from, to, pageRequest);
    }
}
