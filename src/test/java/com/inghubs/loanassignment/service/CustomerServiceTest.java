package com.inghubs.loanassignment.service;

import com.inghubs.loanassignment.dto.CustomerRequest;
import com.inghubs.loanassignment.entity.Customer;
import com.inghubs.loanassignment.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerServiceTest {

    private CustomerRepository customerRepository;
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerRepository = mock(CustomerRepository.class);
        customerService = new CustomerService(customerRepository);
    }

    @Test
    void createCustomer_shouldSaveAndReturnCustomer() {
        CustomerRequest request = new CustomerRequest(1L, "Name", "SurName", BigDecimal.valueOf(1000), BigDecimal.valueOf(0));
        Customer savedCustomer = new Customer();
        savedCustomer.setId(1L);
        savedCustomer.setName("John");
        savedCustomer.setSurname("Doe");
        savedCustomer.setCreditLimit(BigDecimal.valueOf(1000));
        savedCustomer.setUsedCreditLimit(BigDecimal.valueOf(1000));

        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        Customer result = customerService.createCustomer(request);

        assertEquals("John", result.getName());
        assertEquals("Doe", result.getSurname());
        assertEquals(BigDecimal.valueOf(1000), result.getCreditLimit());
        assertEquals(BigDecimal.valueOf(1000), result.getUsedCreditLimit());

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());
    }

    @Test
    void getAllCustomers_shouldReturnCustomerList() {
        Customer c1 = new Customer();
        c1.setName("John");
        Customer c2 = new Customer();
        c2.setName("Jane");

        when(customerRepository.findAll()).thenReturn(Arrays.asList(c1, c2));

        List<Customer> customers = customerService.getAllCustomers();

        assertEquals(2, customers.size());
        assertEquals("John", customers.get(0).getName());
    }

    @Test
    void getCustomerById_shouldReturnCustomerIfExists() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("Alice");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        Optional<Customer> result = customerService.getCustomerById(1L);

        assertTrue(result.isPresent());
        assertEquals("Alice", result.get().getName());
    }

    @Test
    void getCustomerById_shouldReturnEmptyIfNotFound() {
        when(customerRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<Customer> result = customerService.getCustomerById(2L);

        assertFalse(result.isPresent());
    }

    @Test
    void updateCustomer_shouldUpdateAndReturnCustomerIfExists() {
        Customer existing = new Customer();
        existing.setId(1L);
        existing.setName("Old");
        existing.setSurname("Name");
        existing.setCreditLimit(BigDecimal.valueOf(5000));

        CustomerRequest request = new CustomerRequest(1L, "Name", "SurName", BigDecimal.valueOf(1000), BigDecimal.valueOf(0));
        Customer updated = new Customer();
        updated.setId(1L);
        updated.setName("New");
        updated.setSurname("Name");
        updated.setCreditLimit(BigDecimal.valueOf(10000));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(customerRepository.save(any(Customer.class))).thenReturn(updated);

        Customer result = customerService.updateCustomer(1L, request);

        assertEquals("New", result.getName());
        assertEquals("Name", result.getSurname());
        assertEquals(BigDecimal.valueOf(10000), result.getCreditLimit());
    }

    @Test
    void updateCustomer_shouldThrowExceptionIfNotFound() {
        CustomerRequest request = new CustomerRequest(1L, "Name", "SurName", BigDecimal.valueOf(1000), BigDecimal.valueOf(0));
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            customerService.updateCustomer(99L, request);
        });

        assertEquals("Customer not found", ex.getMessage());
    }

    @Test
    void deleteCustomer_shouldCallRepositoryDeleteById() {
        customerService.deleteCustomer(5L);
        verify(customerRepository, times(1)).deleteById(5L);
    }
}
