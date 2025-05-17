package com.inghubs.loanassignment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inghubs.loanassignment.dto.CreateLoanRequest;
import com.inghubs.loanassignment.dto.PayLoanRequest;
import com.inghubs.loanassignment.entity.Loan;
import com.inghubs.loanassignment.entity.LoanInstallment;

import com.inghubs.loanassignment.service.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LoanControllerTest {
    @InjectMocks
    private LoanController loanController;

    @Mock
    private LoanService loanService;

    @BeforeEach
    void setUp() {
        loanService = mock(LoanService.class);
        loanController = new LoanController(loanService);
    }

    @Test
    @WithMockUser(roles = "client_admin")
    void createLoan_shouldReturnLoan() throws Exception {
        CreateLoanRequest request = new CreateLoanRequest();
        Loan loan = new Loan();

        when(loanService.createLoan(any(CreateLoanRequest.class))).thenReturn(loan);

        ResponseEntity<Loan> response = loanController.createLoan(request);


        assertNotNull(response);
    }

    @Test
    @WithMockUser(roles = "client_admin")
    void listLoans_shouldReturnLoanList() throws Exception {
        long customerId = 1L;
        Loan loan = new Loan();
        List<Loan> loans = Collections.singletonList(loan);

        when(loanService.listLoans(customerId)).thenReturn(loans);

        ResponseEntity<List<Loan>> loans1 = loanController.listLoans(customerId);
        verify(loanService).listLoans(customerId);
        assertEquals(loans, loans1.getBody());

    }

    @Test
    @WithMockUser(roles = "client_admin")
    void listInstallments_shouldReturnInstallmentList() throws Exception {
        long loanId = 1L;
        LoanInstallment installment = new LoanInstallment();
        List<LoanInstallment> installments = Collections.singletonList(installment);

        when(loanService.listInstallments(loanId)).thenReturn(installments);

        ResponseEntity<List<LoanInstallment>> loanInstallments = loanController.listInstallments(loanId);
        verify(loanService).listInstallments(loanId);
        assertEquals(installments, loanInstallments.getBody());
    }

    @Test
    @WithMockUser(roles = "client_admin")
    void payLoan_shouldReturnConfirmationMessage() throws Exception {
        PayLoanRequest request = new PayLoanRequest();
        when(loanService.payLoan(any(PayLoanRequest.class))).thenReturn("Payment successful");

        ResponseEntity<String> s = loanController.payLoan(request);

        assertEquals("Payment successful", s.getBody());
    }
}
