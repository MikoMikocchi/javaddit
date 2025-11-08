package com.example.javaddit.core.exception;

/**
 * Exception thrown when validation fails for business logic or domain rules.
 * Results in HTTP 400 Bad Request.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
