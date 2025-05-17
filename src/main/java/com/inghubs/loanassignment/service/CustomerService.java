package com.inghubs.loanassignment.service;


import com.inghubs.loanassignment.dto.CustomerRequest;
import com.inghubs.loanassignment.entity.Customer;
import com.inghubs.loanassignment.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer createCustomer(CustomerRequest dto) {
        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setSurname(dto.getSurname());
        customer.setCreditLimit(dto.getCreditLimit());
        customer.setUsedCreditLimit(dto.getUsedCreditLimit());
        return customerRepository.save(customer);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public Customer updateCustomer(Long id, CustomerRequest dto) {
        return customerRepository.findById(id).map(existing -> {
            existing.setName(dto.getName());
            existing.setSurname(dto.getSurname());
            existing.setCreditLimit(dto.getCreditLimit());
            return customerRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }
}