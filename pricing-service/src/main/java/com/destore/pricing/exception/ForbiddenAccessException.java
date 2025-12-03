package com.destore.pricing.exception;

/**
 * @file ForbiddenAccessException.java
 * @brief Exception for authorization failures
 * 
 * Thrown when a user attempts to access or modify a resource
 * they don't have permission for (e.g., store manager trying
 * to edit global rules or another store's rules).
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
public class ForbiddenAccessException extends RuntimeException {
    
    public ForbiddenAccessException(String message) {
        super(message);
    }
    
    public ForbiddenAccessException(String operation, String reason) {
        super(String.format("Forbidden: Cannot %s - %s", operation, reason));
    }
}
