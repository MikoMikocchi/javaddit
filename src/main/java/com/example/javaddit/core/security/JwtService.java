package com.example.javaddit.core.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class JwtService {

    public static final String CLAIM_USER_ID = "uid";
    public static final String CLAIM_ROLES = "roles";

    private final JwtProperties properties;
    private final SecretKey secretKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UserPrincipal principal) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(properties.accessTokenTtlSeconds());

    return Jwts.builder()
        .issuer(properties.issuer())
        .subject(principal.getUsername())
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiry))
        .claims(buildClaims(principal))
        .signWith(secretKey, Jwts.SIG.HS256)
        .compact();
    }

    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException ex) {
            throw new InvalidJwtException("Failed to parse JWT", ex);
        }
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        Claims claims = parseClaims(token);
        Long asLong = claims.get(CLAIM_USER_ID, Long.class);
        if (asLong != null) {
            return asLong;
        }
        Integer asInt = claims.get(CLAIM_USER_ID, Integer.class);
        if (asInt != null) {
            return asInt.longValue();
        }
        String asString = claims.get(CLAIM_USER_ID, String.class);
        if (asString != null) {
            try {
                return Long.valueOf(asString);
            } catch (NumberFormatException ex) {
                throw new InvalidJwtException("JWT user id claim is not a valid number", ex);
            }
        }
        throw new InvalidJwtException("JWT missing user id claim");
    }

    public boolean isExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration == null || expiration.toInstant().isBefore(Instant.now());
    }

    private Map<String, Object> buildClaims(UserPrincipal principal) {
        List<String> roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Map.of(
                CLAIM_USER_ID, principal.getId(),
                CLAIM_ROLES, roles
        );
    }

    public static class InvalidJwtException extends RuntimeException {
        public InvalidJwtException(String message, Throwable cause) {
            super(message, cause);
        }

        public InvalidJwtException(String message) {
            super(message);
        }
    }
}
