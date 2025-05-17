package com.inghubs.loanassignment.repository;

import com.inghubs.loanassignment.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
