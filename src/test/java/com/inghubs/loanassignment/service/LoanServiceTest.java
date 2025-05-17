package com.inghubs.loanassignment.service;

import com.inghubs.loanassignment.dto.CreateLoanRequest;
import com.inghubs.loanassignment.dto.PayLoanRequest;
import com.inghubs.loanassignment.entity.Customer;
import com.inghubs.loanassignment.entity.Loan;
import com.inghubs.loanassignment.entity.LoanInstallment;
import com.inghubs.loanassignment.repository.CustomerRepository;
import com.inghubs.loanassignment.repository.LoanInstallmentRepository;
import com.inghubs.loanassignment.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LoanServiceTest {

    private CustomerRepository customerRepo;
    private LoanRepository loanRepo;
    private LoanInstallmentRepository installmentRepo;
    private LoanService loanService;

    @BeforeEach
    void setUp() {
        customerRepo = mock(CustomerRepository.class);
        loanRepo = mock(LoanRepository.class);
        installmentRepo = mock(LoanInstallmentRepository.class);
        loanService = new LoanService(customerRepo, loanRepo, installmentRepo);
    }

    @Test
    void createLoan_shouldThrow_whenCustomerNotFound() {
        CreateLoanRequest req = new CreateLoanRequest();
        req.setCustomerId(1L);
        when(customerRepo.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> loanService.createLoan(req));
        assertEquals("Customer not found", ex.getMessage());
    }

    @Test
    void createLoan_shouldThrow_whenInvalidInstallment() {
        CreateLoanRequest req = new CreateLoanRequest();
        req.setCustomerId(1L);
        req.setInstallments(5);
        Customer c = new Customer();
        when(customerRepo.findById(1L)).thenReturn(Optional.of(c));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> loanService.createLoan(req));
        assertEquals("Invalid installment count", ex.getMessage());
    }

    @Test
    void createLoan_shouldThrow_whenInvalidInterestRate() {
        CreateLoanRequest req = new CreateLoanRequest();
        req.setCustomerId(1L);
        req.setInstallments(12);
        req.setInterestRate(0.6);
        Customer c = new Customer();
        when(customerRepo.findById(1L)).thenReturn(Optional.of(c));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> loanService.createLoan(req));
        assertEquals("Invalid interest rate", ex.getMessage());
    }

    @Test
    void createLoan_shouldThrow_whenInsufficientCreditLimit() {
        CreateLoanRequest req = new CreateLoanRequest();
        req.setCustomerId(1L);
        req.setInstallments(12);
        req.setInterestRate(0.2);
        req.setAmount(BigDecimal.valueOf(1000));

        Customer c = new Customer();
        c.setCreditLimit(BigDecimal.valueOf(1100));
        c.setUsedCreditLimit(BigDecimal.valueOf(1000));
        when(customerRepo.findById(1L)).thenReturn(Optional.of(c));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> loanService.createLoan(req));
        assertEquals("Insufficient credit limit", ex.getMessage());
    }

    @Test
    void createLoan_shouldCreateLoanAndInstallments_whenValidRequest() {
        CreateLoanRequest req = new CreateLoanRequest();
        req.setCustomerId(1L);
        req.setInstallments(6);
        req.setInterestRate(0.2);
        req.setAmount(BigDecimal.valueOf(1000));

        Customer c = new Customer();
        c.setCreditLimit(BigDecimal.valueOf(2000));
        c.setUsedCreditLimit(BigDecimal.ZERO);
        when(customerRepo.findById(1L)).thenReturn(Optional.of(c));

        when(loanRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(installmentRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        Loan result = loanService.createLoan(req);

        assertEquals(BigDecimal.valueOf(1200.0), result.getLoanAmount());
        verify(installmentRepo, times(6)).save(any(LoanInstallment.class));
    }

    @Test
    void listLoans_shouldReturnList() {
        Loan loan = new Loan();
        when(loanRepo.findByCustomerId(1L)).thenReturn(List.of(loan));

        List<Loan> loans = loanService.listLoans(1L);

        assertEquals(1, loans.size());
    }

    @Test
    void listInstallments_shouldReturnList() {
        LoanInstallment installment = new LoanInstallment();
        when(installmentRepo.findByLoanIdOrderByDueDateAsc(1L)).thenReturn(List.of(installment));

        List<LoanInstallment> list = loanService.listInstallments(1L);
        assertEquals(1, list.size());
    }

    @Test
    void payLoan_shouldPayInstallments_andReturnCorrectMessage() {
        Loan loan = new Loan();
        loan.setId(1L);
        loan.setPaid(false);

        PayLoanRequest request = new PayLoanRequest();
        request.setLoanId(1L);
        request.setAmount(BigDecimal.valueOf(500));

        LoanInstallment inst1 = new LoanInstallment();
        inst1.setAmount(BigDecimal.valueOf(200));
        inst1.setDueDate(LocalDate.now().minusDays(2));
        inst1.setPaid(false);

        LoanInstallment inst2 = new LoanInstallment();
        inst2.setAmount(BigDecimal.valueOf(200));
        inst2.setDueDate(LocalDate.now());
        inst2.setPaid(false);

        LoanInstallment inst3 = new LoanInstallment();
        inst3.setAmount(BigDecimal.valueOf(200));
        inst3.setDueDate(LocalDate.now().plusMonths(4));
        inst3.setPaid(false); // won't be paid

        when(loanRepo.findById(1L)).thenReturn(Optional.of(loan));
        when(installmentRepo.findByLoanIdOrderByDueDateAsc(1L)).thenReturn(List.of(inst1, inst2, inst3));

        String result = loanService.payLoan(request);

        assertTrue(inst1.isPaid());
        assertTrue(inst2.isPaid());
        assertFalse(inst3.isPaid());
        assertTrue(result.contains("Paid 2 installments"));
        verify(installmentRepo, times(2)).save(any());
    }

    @Test
    void payLoan_shouldMarkLoanAsPaid_whenAllInstallmentsPaid() {
        Loan loan = new Loan();
        loan.setId(1L);
        loan.setPaid(false);

        PayLoanRequest request = new PayLoanRequest();
        request.setLoanId(1L);
        request.setAmount(BigDecimal.valueOf(1000));

        List<LoanInstallment> installments = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            LoanInstallment inst = new LoanInstallment();
            inst.setAmount(BigDecimal.valueOf(100));
            inst.setDueDate(LocalDate.now().minusDays(i * 10));
            inst.setPaid(false);
            installments.add(inst);
        }

        when(loanRepo.findById(1L)).thenReturn(Optional.of(loan));
        when(installmentRepo.findByLoanIdOrderByDueDateAsc(1L)).thenReturn(installments);

        String result = loanService.payLoan(request);

        assertTrue(loan.isPaid());
        assertTrue(result.contains("Loan fully paid: true"));
        verify(loanRepo).save(loan);
    }
}

