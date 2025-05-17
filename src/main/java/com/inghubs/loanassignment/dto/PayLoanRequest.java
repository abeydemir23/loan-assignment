package com.inghubs.loanassignment.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayLoanRequest {
    public Long loanId;
    public BigDecimal amount;
}
