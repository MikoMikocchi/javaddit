package com.example.javaddit.core.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Unified error response envelope for consistent API error handling.
 * All error responses from the API will use this structure.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorEnvelope {

    /**
     * HTTP status code
     */
    private int status;

    /**
     * General error message describing what went wrong
     */
    private String message;

    /**
     * Detailed field-level validation errors (for validation failures)
     * Key: field name, Value: error message
     */
    private Map<String, String> errors;

    /**
     * Timestamp when the error occurred
     */
    private LocalDateTime timestamp;

    /**
     * Constructor for simple errors without field-level details
     */
    public ErrorEnvelope(int status, String message, LocalDateTime timestamp) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }

    /**
     * Constructor for validation errors with field-level details
     */
    public ErrorEnvelope(int status, String message, Map<String, String> errors, LocalDateTime timestamp) {
        this.status = status;
        this.message = message;
        this.errors = errors;
        this.timestamp = timestamp;
    }
}
