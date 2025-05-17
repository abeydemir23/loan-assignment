package com.inghubs.loanassignment.service;


import com.inghubs.loanassignment.dto.CreateLoanRequest;
import com.inghubs.loanassignment.dto.PayLoanRequest;
import com.inghubs.loanassignment.entity.Customer;
import com.inghubs.loanassignment.entity.Loan;
import com.inghubs.loanassignment.entity.LoanInstallment;
import com.inghubs.loanassignment.repository.CustomerRepository;
import com.inghubs.loanassignment.repository.LoanInstallmentRepository;
import com.inghubs.loanassignment.repository.LoanRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class LoanService {
    private final CustomerRepository customerRepo;
    private final LoanRepository loanRepo;
    private final LoanInstallmentRepository installmentRepo;

    public LoanService(CustomerRepository customerRepo, LoanRepository loanRepo, LoanInstallmentRepository installmentRepo) {
        this.customerRepo = customerRepo;
        this.loanRepo = loanRepo;
        this.installmentRepo = installmentRepo;
    }

    @Transactional
    public Loan createLoan(CreateLoanRequest request) {
        return handleCreateLoan(request);
    }

    private Loan handleCreateLoan(CreateLoanRequest request) {
        Customer customer = customerRepo.findById(request.getCustomerId()).orElseThrow(()-> new RuntimeException("Customer not found"));
        if (request.getInstallments() != 6 && request.getInstallments() != 9 && request.getInstallments() != 12 && request.getInstallments() != 24)
            throw new IllegalArgumentException("Invalid installment count");
        if (request.getInterestRate() < 0.1 || request.getInterestRate() > 0.5)
            throw new IllegalArgumentException("Invalid interest rate");

        BigDecimal totalAmount = request.getAmount().multiply(BigDecimal.valueOf(1 + request.interestRate));
        if (customer.getCreditLimit().subtract(customer.getUsedCreditLimit()).compareTo(totalAmount) < 0)
            throw new IllegalArgumentException("Insufficient credit limit");

        customer.setUsedCreditLimit(customer.getUsedCreditLimit().add(totalAmount));

        Loan loan = new Loan();
        loan.setCustomer(customer);
        loan.setLoanAmount(totalAmount);
        loan.setNumberOfInstallments(request.getInstallments());
        loan.setCreateDate(LocalDate.now());
        loanRepo.save(loan);

        BigDecimal installmentAmount = totalAmount.divide(BigDecimal.valueOf(request.getInstallments()), BigDecimal.ROUND_HALF_UP);
        LocalDate dueDate = LocalDate.now().with(TemporalAdjusters.firstDayOfNextMonth());
        for (int i = 0; i < request.getInstallments(); i++) {
            LoanInstallment inst = new LoanInstallment();
            inst.setLoan(loan);
            inst.setAmount(installmentAmount);
            inst.setDueDate(dueDate.plusMonths(i));
            installmentRepo.save(inst);
        }
        return loan;
    }

    public List<Loan> listLoans(Long customerId) {
        return loanRepo.findByCustomerId(customerId);
    }

    public List<LoanInstallment> listInstallments(Long loanId) {
        return installmentRepo.findByLoanIdOrderByDueDateAsc(loanId);
    }

    @Transactional
    public String payLoan(PayLoanRequest request) {
        Loan loan = loanRepo.findById(request.getLoanId()).orElseThrow();
        Result result = handleLoanPayment(request, loan);
        loanRepo.save(loan);
        return String.format("Paid %d installments. Remaining amount: %s. Loan fully paid: %b", result.paidCount(), result.amountToPay(), result.allPaid());
    }

    private Result handleLoanPayment(PayLoanRequest request, Loan loan) {
        List<LoanInstallment> installments = installmentRepo.findByLoanIdOrderByDueDateAsc(loan.getId());

        BigDecimal amountToPay = request.getAmount();
        int paidCount = 0;
        LocalDate now = LocalDate.now();

        for (LoanInstallment inst : installments) {
            if (inst.isPaid()) continue;
            if (inst.getDueDate().isAfter(now.plusMonths(3))) continue;

            BigDecimal finalAmount = inst.getAmount();
            long days = java.time.temporal.ChronoUnit.DAYS.between(now, inst.getDueDate());
            if (days > 0)
                finalAmount = finalAmount.subtract(inst.getAmount().multiply(BigDecimal.valueOf(0.001 * days)));
            else if (days < 0)
                finalAmount = finalAmount.add(inst.getAmount().multiply(BigDecimal.valueOf(0.001 * -days)));

            if (amountToPay.compareTo(finalAmount) >= 0) {
                amountToPay = amountToPay.subtract(finalAmount);
                inst.setPaid(true);
                inst.setPaidAmount(finalAmount);
                inst.setPaymentDate(now);
                installmentRepo.save(inst);
                paidCount++;
            } else {
                break;
            }
        }

        boolean allPaid = installments.stream().allMatch(LoanInstallment::isPaid);
        if (allPaid) loan.setPaid(true);
        return new Result(amountToPay, paidCount, allPaid);
    }

    private record Result(BigDecimal amountToPay, int paidCount, boolean allPaid) {
    }
}
