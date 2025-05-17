package com.inghubs.loanassignment.controller;

import com.inghubs.loanassignment.dto.CustomerRequest;
import com.inghubs.loanassignment.entity.Customer;
import com.inghubs.loanassignment.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerControllerTest {

    private CustomerService customerService;
    private CustomerController customerController;

    @BeforeEach
    void setUp() {
        customerService = mock(CustomerService.class);
        customerController = new CustomerController(customerService);
    }

    @Test
    @WithMockUser(roles = "client_admin")
    void createCustomer_shouldReturnCreatedCustomer() {
        CustomerRequest request = new CustomerRequest();
        Customer createdCustomer = new Customer();

        when(customerService.createCustomer(request)).thenReturn(createdCustomer);

        ResponseEntity<Customer> response = customerController.createCustomer(request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(createdCustomer, response.getBody());
        verify(customerService).createCustomer(request);
    }

    @Test
    @WithMockUser(roles = "client_admin")
    void getAllCustomers_shouldReturnListOfCustomers() {
        List<Customer> customers = Arrays.asList(new Customer(), new Customer());
        when(customerService.getAllCustomers()).thenReturn(customers);

        ResponseEntity<List<Customer>> response = customerController.getAllCustomers();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
        verify(customerService).getAllCustomers();
    }

    @Test
    @WithMockUser(roles = "client_admin")
    void getCustomerById_shouldReturnCustomerIfExists() {
        Customer customer = new Customer();
        when(customerService.getCustomerById(1L)).thenReturn(Optional.of(customer));

        ResponseEntity<Customer> response = customerController.getCustomerById(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(customer, response.getBody());
        verify(customerService).getCustomerById(1L);
    }

    @Test
    @WithMockUser(roles = "client_admin")
    void getCustomerById_shouldReturnNotFoundIfCustomerDoesNotExist() {
        when(customerService.getCustomerById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Customer> response = customerController.getCustomerById(1L);

        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());
        verify(customerService).getCustomerById(1L);
    }

    @Test
    @WithMockUser(roles = "client_admin")
    void updateCustomer_shouldReturnUpdatedCustomer() {
        CustomerRequest request = new CustomerRequest();
        Customer updatedCustomer = new Customer();

        when(customerService.updateCustomer(eq(1L), eq(request))).thenReturn(updatedCustomer);

        ResponseEntity<Customer> response = customerController.updateCustomer(1L, request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(updatedCustomer, response.getBody());
        verify(customerService).updateCustomer(1L, request);
    }

    @Test
    @WithMockUser(roles = "client_admin")
    void deleteCustomer_shouldReturnNoContent() {
        doNothing().when(customerService).deleteCustomer(1L);

        ResponseEntity<Void> response = customerController.deleteCustomer(1L);

        assertEquals(204, response.getStatusCodeValue());
        verify(customerService).deleteCustomer(1L);
    }
}