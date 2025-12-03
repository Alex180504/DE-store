package com.destore.store.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @file ForbiddenAccessException.java
 * @brief Exception for forbidden access attempts
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenAccessException extends RuntimeException {
    public ForbiddenAccessException(String message) {
        super(message);
    }
}
