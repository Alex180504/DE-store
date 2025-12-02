package com.destore.pricing.controller;

import com.destore.pricing.exception.ItemNotFoundException;
import com.destore.pricing.exception.PricingRuleNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @file GlobalExceptionHandler.java
 * @brief Global exception handler for REST API
 * 
 * Provides centralized exception handling for all controllers.
 * Converts exceptions into standardized error responses with appropriate
 * HTTP status codes.
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * @brief Handle item not found exceptions
     * @param ex The exception
     * @return Error response with 404 status
     */
    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleItemNotFound(ItemNotFoundException ex) {
        log.error("Item not found: {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * @brief Handle pricing rule not found exceptions
     * @param ex The exception
     * @return Error response with 404 status
     */
    @ExceptionHandler(PricingRuleNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePricingRuleNotFound(PricingRuleNotFoundException ex) {
        log.error("Pricing rule not found: {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * @brief Handle validation errors
     * @param ex The validation exception
     * @return Error response with 400 status and field errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        log.error("Validation errors: {}", fieldErrors);

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("fieldErrors", fieldErrors);

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * @brief Handle all other exceptions
     * @param ex The exception
     * @return Error response with 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);
        return buildErrorResponse("Internal server error: " + ex.getMessage(), 
                                  HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * @brief Build standardized error response
     * @param message Error message
     * @param status HTTP status code
     * @return Error response map
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }
}
