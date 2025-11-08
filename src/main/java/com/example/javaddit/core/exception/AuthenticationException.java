package com.example.javaddit.core.exception;

/**
 * Exception thrown when authentication fails.
 * For example, invalid credentials, expired tokens, etc.
 * Results in HTTP 401 Unauthorized.
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
