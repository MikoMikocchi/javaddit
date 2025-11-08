package com.example.javaddit.core.util;

import lombok.experimental.UtilityClass;

/**
 * Utility class for normalizing string inputs in authentication and user-related operations.
 * Centralizes normalization logic to ensure consistency across the application.
 */
@UtilityClass
public class StringNormalizer {

    /**
     * Normalizes a username by trimming whitespace.
     *
     * @param username the raw username input
     * @return normalized username, or null if input is null
     */
    public static String normalizeUsername(String username) {
        return username != null ? username.trim() : null;
    }

    /**
     * Normalizes an email address by trimming whitespace and converting to lowercase.
     *
     * @param email the raw email input
     * @return normalized email, or null if input is null
     */
    public static String normalizeEmail(String email) {
        return email != null ? email.trim().toLowerCase() : null;
    }

    /**
     * Normalizes a login identifier (username or email) by trimming whitespace.
     *
     * @param identifier the raw identifier input
     * @return normalized identifier, or null if input is null
     */
    public static String normalizeIdentifier(String identifier) {
        return identifier != null ? identifier.trim() : null;
    }

    /**
     * Normalizes a token by trimming whitespace.
     *
     * @param token the raw token input
     * @return normalized token, or null if input is null
     */
    public static String normalizeToken(String token) {
        return token != null ? token.trim() : null;
    }
}
