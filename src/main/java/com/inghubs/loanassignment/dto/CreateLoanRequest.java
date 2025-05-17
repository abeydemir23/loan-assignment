package com.inghubs.loanassignment.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateLoanRequest {
    public Long customerId;
    public BigDecimal amount;
    public double interestRate;
    public int installments;
}
