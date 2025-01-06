package com.ekene.servicebackendfintech.service;

import com.ekene.servicebackendfintech.transaction.enums.TransactionType;
import com.ekene.servicebackendfintech.transaction.model.Transaction;
import com.ekene.servicebackendfintech.transaction.payload.TransactionRequest;
import com.ekene.servicebackendfintech.transaction.repository.TransactionRepository;
import com.ekene.servicebackendfintech.transaction.service.TransactionService;
import com.ekene.servicebackendfintech.utils.ApiResponse;
import com.ekene.servicebackendfintech.utils.ValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ValidationService validationService;

    @InjectMocks
    private TransactionService transactionService;

    private TransactionRequest validTransactionRequest;
    private String userId;
    private Transaction mockTransaction;

    @BeforeEach
    void setUp() {
        userId = "USER123";
        validTransactionRequest = new TransactionRequest(
                "TRANS123",
                new BigDecimal("1000"),
                TransactionType.DISBURSEMENT
        );

        mockTransaction = Transaction.builder()
                .userId(userId)
                .transactionReference("TRANS123")
                .amount(new BigDecimal("1000"))
                .type(TransactionType.DISBURSEMENT)
                .transactionDate(LocalDate.now())
                .build();
    }

    @Nested
    @DisplayName("Record Transaction Tests")
    class RecordTransactionTests {

        @Test
        @DisplayName("Should successfully record a disbursement transaction")
        void shouldRecordDisbursementTransaction() {
            doNothing().when(validationService).validateTransaction(any(), anyString());
            when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);

            transactionService.recordTransaction(validTransactionRequest, userId);

            ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
            verify(transactionRepository).save(transactionCaptor.capture());

            Transaction savedTransaction = transactionCaptor.getValue();
            assertEquals(validTransactionRequest.getTransactionReference(), savedTransaction.getTransactionReference());
            assertEquals(validTransactionRequest.getAmount(), savedTransaction.getAmount());
            assertEquals(TransactionType.DISBURSEMENT, savedTransaction.getType());
            assertFalse(savedTransaction.isRepaid());
        }

        @Test
        @DisplayName("Should successfully record a repayment transaction")
        void shouldRecordRepaymentTransaction() {
            validTransactionRequest.setType(TransactionType.REPAYMENT);
            doNothing().when(validationService).validateTransaction(any(), anyString());
            when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);

            transactionService.recordTransaction(validTransactionRequest, userId);

            ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
            verify(transactionRepository).save(transactionCaptor.capture());

            Transaction savedTransaction = transactionCaptor.getValue();
            assertTrue(savedTransaction.isRepaid());
            assertEquals(TransactionType.REPAYMENT, savedTransaction.getType());
        }

        @Test
        @DisplayName("Should validate transaction before recording")
        void shouldValidateTransactionBeforeRecording() {
            transactionService.recordTransaction(validTransactionRequest, userId);

            verify(validationService, times(1)).validateTransaction(validTransactionRequest, userId);
        }
    }

    @Nested
    @DisplayName("Generate Statement Tests")
    class GenerateStatementTests {

        @Test
        @DisplayName("Should generate statement for given date range")
        void shouldGenerateStatementForDateRange() {
            LocalDate from = LocalDate.now().minusMonths(1);
            LocalDate to = LocalDate.now();
            int size = 0;
            int length = 10;

            List<Transaction> transactions = Arrays.asList(mockTransaction);
            Page<Transaction> transactionPage = new PageImpl<>(transactions);

            when(transactionRepository.findByUserIdAndDateRange(
                    eq(userId), eq(from), eq(to), any(PageRequest.class)))
                    .thenReturn(transactionPage);

            ResponseEntity<ApiResponse<Page<Transaction>>> response =
                    transactionService.generateStatement(userId, from, to, size, length);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Successfully Fetched User statement", response.getBody().getMessage());
            assertEquals(transactionPage, response.getBody().getData());
        }

        @Test
        @DisplayName("Should handle empty statement")
        void shouldHandleEmptyStatement() {
            LocalDate from = LocalDate.now().minusMonths(1);
            LocalDate to = LocalDate.now();
            Page<Transaction> emptyPage = new PageImpl<>(List.of());

            when(transactionRepository.findByUserIdAndDateRange(
                    anyString(), any(), any(), any(PageRequest.class)))
                    .thenReturn(emptyPage);

            ResponseEntity<ApiResponse<Page<Transaction>>> response =
                    transactionService.generateStatement(userId, from, to, 0, 10);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().getData().isEmpty());
        }
    }
}
