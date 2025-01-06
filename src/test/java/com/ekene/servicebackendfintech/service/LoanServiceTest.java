package com.ekene.servicebackendfintech.service;

import com.ekene.servicebackendfintech.exception.LoanNotFoundException;
import com.ekene.servicebackendfintech.loan.enums.LoanStatus;
import com.ekene.servicebackendfintech.loan.model.Loan;
import com.ekene.servicebackendfintech.loan.payload.LoanRequest;
import com.ekene.servicebackendfintech.loan.payload.LoanResponse;
import com.ekene.servicebackendfintech.loan.repository.LoanRepository;
import com.ekene.servicebackendfintech.loan.service.LoanService;
import com.ekene.servicebackendfintech.transaction.service.TransactionService;
import com.ekene.servicebackendfintech.user.model.FintechUser;
import com.ekene.servicebackendfintech.user.repository.UserRepository;
import com.ekene.servicebackendfintech.utils.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private LoanService loanService;

    private FintechUser testUser;
    private LoanRequest validLoanRequest;
    private Loan existingLoan;

    @BeforeEach
    void setUp() {
        testUser = FintechUser.builder()
                .userId("USER123")
                .email("test@example.com")
                .phoneNumber("1234567890")
                .build();

        validLoanRequest = new LoanRequest();
        validLoanRequest.setAmount(new BigDecimal("10000"));
        validLoanRequest.setTenure(12);
        validLoanRequest.setTransactionReference("LOAN123");

        existingLoan = new Loan();
        existingLoan.setId(1L);
        existingLoan.setUserEmail(testUser.getEmail());
        existingLoan.setPrincipalAmount(new BigDecimal("10000"));
        existingLoan.setStatus(LoanStatus.APPROVED);
    }

    @Nested
    @DisplayName("Apply for Loan Tests")
    class ApplyForLoanTests {

        @Test
        @DisplayName("Should successfully create a new loan when user has no outstanding loans")
        void shouldCreateNewLoanSuccessfully() {
            when(userRepository.findByEmailIgnoreCase(anyString()))
                    .thenReturn(Optional.of(testUser));
            when(loanRepository.findMostRecentLoanByUserEmail(anyString()))
                    .thenReturn(null);
            when(loanRepository.save(any(Loan.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ResponseEntity<ApiResponse<LoanResponse>> response =
                    loanService.applyForLoan(validLoanRequest, testUser.getEmail());

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Successfully applied for loan", response.getBody().getMessage());
            verify(transactionService, times(1)).recordTransaction(any(), anyString());
        }

        @Test
        @DisplayName("Should reject loan when user has outstanding loan")
        void shouldRejectLoanWhenOutstandingLoanExists() {
            when(userRepository.findByEmailIgnoreCase(anyString()))
                    .thenReturn(Optional.of(testUser));
            when(loanRepository.findMostRecentLoanByUserEmail(anyString()))
                    .thenReturn(existingLoan);

            ResponseEntity<ApiResponse<LoanResponse>> response =
                    loanService.applyForLoan(validLoanRequest, testUser.getEmail());

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("User has an outstanding loan", response.getBody().getMessage());
            assertNull(response.getBody().getData());
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            when(userRepository.findByEmailIgnoreCase(anyString()))
                    .thenReturn(Optional.empty());

            assertThrows(UsernameNotFoundException.class,
                    () -> loanService.applyForLoan(validLoanRequest, "nonexistent@example.com"));
        }
    }

    @Nested
    @DisplayName("Loan Repayment Tests")
    class LoanRepaymentTests {

        @Test
        @DisplayName("Should successfully process loan repayment")
        void shouldProcessRepaymentSuccessfully() {
            String transactionRef = "REPAY123";
            BigDecimal repaymentAmount = new BigDecimal("1000");
            existingLoan.setAmountPaid(BigDecimal.ZERO);
            existingLoan.setTotalRepaymentAmount(new BigDecimal("12000"));
            existingLoan.setOutstandingAmount(new BigDecimal("12000"));

            when(userRepository.findByEmailIgnoreCase(anyString()))
                    .thenReturn(Optional.of(testUser));
            when(loanRepository.getLoanByTransactionReferenceIgnoreCase(anyString()))
                    .thenReturn(existingLoan);
            when(loanRepository.save(any(Loan.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ResponseEntity<ApiResponse<LoanResponse>> response =
                    loanService.updateLoanRepayment(transactionRef, repaymentAmount, testUser.getEmail());

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Successfully repayment operation", response.getBody().getMessage());
            verify(transactionService, times(1)).recordTransaction(any(), anyString());
        }

        @Test
        @DisplayName("Should mark loan as completed when fully repaid")
        void shouldMarkLoanAsCompletedWhenFullyRepaid() {
            String transactionRef = "REPAY123";
            BigDecimal repaymentAmount = new BigDecimal("12000");
            existingLoan.setAmountPaid(BigDecimal.ZERO);
            existingLoan.setTotalRepaymentAmount(new BigDecimal("12000"));
            existingLoan.setOutstandingAmount(new BigDecimal("12000"));

            when(userRepository.findByEmailIgnoreCase(anyString()))
                    .thenReturn(Optional.of(testUser));
            when(loanRepository.getLoanByTransactionReferenceIgnoreCase(anyString()))
                    .thenReturn(existingLoan);
            when(loanRepository.save(any(Loan.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ResponseEntity<ApiResponse<LoanResponse>> response =
                    loanService.updateLoanRepayment(transactionRef, repaymentAmount, testUser.getEmail());

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(loanRepository, times(1)).save(argThat(loan ->
                    loan.getStatus() == LoanStatus.COMPLETED));
        }
    }

    @Nested
    @DisplayName("Loan Status Update Tests")
    class LoanStatusUpdateTests {

        @Test
        @DisplayName("Should successfully update loan status")
        void shouldUpdateLoanStatus() {
            Long loanId = 1L;
            when(loanRepository.findById(loanId))
                    .thenReturn(Optional.of(existingLoan));
            when(loanRepository.save(any(Loan.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Loan updatedLoan = loanService.updateLoanStatus(loanId, LoanStatus.COMPLETED);

            assertNotNull(updatedLoan);
            assertEquals(LoanStatus.COMPLETED, updatedLoan.getStatus());
            verify(loanRepository, times(1)).save(any(Loan.class));
        }

        @Test
        @DisplayName("Should throw exception when loan not found for status update")
        void shouldThrowExceptionWhenLoanNotFound() {
            Long loanId = 999L;
            when(loanRepository.findById(loanId))
                    .thenReturn(Optional.empty());

            assertThrows(LoanNotFoundException.class,
                    () -> loanService.updateLoanStatus(loanId, LoanStatus.COMPLETED));
        }
    }

    @Nested
    @DisplayName("Get User Loans Tests")
    class GetUserLoansTests {

        @Test
        @DisplayName("Should successfully retrieve user loan by email and transaction reference")
        void shouldRetrieveUserLoan() {
            String email = "test@example.com";
            String transactionRef = "LOAN123";
            when(loanRepository.findByUserEmailEqualsIgnoreCaseAndTransactionReferenceEqualsIgnoreCase(
                    email, transactionRef))
                    .thenReturn(existingLoan);

            ResponseEntity<ApiResponse<LoanResponse>> response =
                    loanService.getUserLoans(email, transactionRef);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Successfully Fetched loan details", response.getBody().getMessage());
            assertNotNull(response.getBody().getData());
        }
    }
}
