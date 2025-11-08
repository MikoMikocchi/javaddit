package com.example.javaddit.core.exception;

/**
 * Exception thrown when there is a conflict with existing resources.
 * For example, when trying to create a resource that already exists.
 * Results in HTTP 409 Conflict.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
