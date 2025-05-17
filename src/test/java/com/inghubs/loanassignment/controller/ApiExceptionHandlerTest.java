package com.inghubs.loanassignment.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;

public class ApiExceptionHandlerTest {

    private ApiExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ApiExceptionHandler();
    }

    @Test
    void handleNotFoundException_shouldReturnNotFoundStatus() {
        RuntimeException exception = new RuntimeException("Resource not found");

        ResponseEntity<String> response = handler.handleNotFoundException(exception);

        assertEquals(NOT_FOUND, response.getStatusCode());
        assertEquals("Resource not found", response.getBody());
    }

    @Test
    void handleInvalidInputException_shouldReturnBadGatewayStatus() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid input");

        ResponseEntity<String> response = handler.handleInvalidInputException(exception);

        assertEquals(BAD_GATEWAY, response.getStatusCode());
        assertEquals("Invalid input", response.getBody());
    }
}
