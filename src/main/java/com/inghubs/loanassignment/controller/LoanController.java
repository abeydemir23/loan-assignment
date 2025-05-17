package com.inghubs.loanassignment.controller;


import com.inghubs.loanassignment.dto.CreateLoanRequest;
import com.inghubs.loanassignment.dto.PayLoanRequest;
import com.inghubs.loanassignment.entity.Loan;
import com.inghubs.loanassignment.entity.LoanInstallment;
import com.inghubs.loanassignment.service.LoanService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LoanController {
    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping("/loans")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<Loan> createLoan(@RequestBody CreateLoanRequest request) {
        return ResponseEntity.ok().body(loanService.createLoan(request));
    }

    @GetMapping("/loans/{customerId}")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<List<Loan>> listLoans(@PathVariable Long customerId) {
        return ResponseEntity.ok(loanService.listLoans(customerId));
    }

    @GetMapping("/installments/{loanId}")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<List<LoanInstallment>> listInstallments(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.listInstallments(loanId));
    }

    @PostMapping("/payments")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<String> payLoan(@RequestBody PayLoanRequest request) {
        return ResponseEntity.ok(loanService.payLoan(request));
    }
}

