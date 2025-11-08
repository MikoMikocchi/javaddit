package com.example.javaddit.features.auth.service;

import com.example.javaddit.core.exception.AuthenticationException;
import com.example.javaddit.core.exception.ConflictException;
import com.example.javaddit.core.security.JwtProperties;
import com.example.javaddit.core.security.JwtService;
import com.example.javaddit.core.security.UserPrincipal;
import com.example.javaddit.core.util.StringNormalizer;
import com.example.javaddit.features.auth.dto.AuthResponse;
import com.example.javaddit.features.auth.dto.LoginRequest;
import com.example.javaddit.features.auth.dto.RefreshTokenRequest;
import com.example.javaddit.features.auth.dto.RegisterRequest;
import com.example.javaddit.features.user.entity.RefreshToken;
import com.example.javaddit.features.user.entity.Role;
import com.example.javaddit.features.user.entity.User;
import com.example.javaddit.features.user.repository.RefreshTokenRepository;
import com.example.javaddit.features.user.repository.RoleRepository;
import com.example.javaddit.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String DEFAULT_ROLE = "USER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        validateRegistration(request);

        Role role = roleRepository.findById(DEFAULT_ROLE)
                .orElseThrow(() -> new IllegalStateException("Default role USER is not configured"));

        String normalizedUsername = StringNormalizer.normalizeUsername(request.username());
        String normalizedEmail = StringNormalizer.normalizeEmail(request.email());

        User user = new User();
        user.setUsername(normalizedUsername);
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setIsDeleted(false);
        user.getRoles().add(role);

        User saved = userRepository.save(user);
        UserPrincipal principal = UserPrincipal.fromUser(saved);
        return issueTokens(principal);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String identifier = StringNormalizer.normalizeIdentifier(request.identifier());

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                identifier,
                request.password()
        );

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(authToken);
        } catch (org.springframework.security.core.AuthenticationException ex) {
            throw new AuthenticationException("Invalid credentials", ex);
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return issueTokens(principal);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        String tokenValue = StringNormalizer.normalizeToken(request.refreshToken());

        RefreshToken stored = refreshTokenRepository.findByTokenAndRevokedFalse(tokenValue)
                .orElseThrow(() -> new AuthenticationException("Invalid refresh token"));

        if (stored.getExpiresAt().isBefore(Instant.now())) {
            stored.setRevoked(true);
            stored.setRevokedAt(Instant.now());
            refreshTokenRepository.save(stored);
            throw new AuthenticationException("Refresh token expired");
        }

        stored.setRevoked(true);
        stored.setRevokedAt(Instant.now());
        refreshTokenRepository.save(stored);

        User user = stored.getUser();
        UserPrincipal principal = UserPrincipal.fromUser(user);
        return issueTokens(principal);
    }

    private AuthResponse issueTokens(UserPrincipal principal) {
        String accessToken = jwtService.generateAccessToken(principal);
        String refreshToken = persistRefreshToken(principal.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.accessTokenTtlSeconds())
                .build();
    }

    private String persistRefreshToken(Long userId) {
        Long resolvedUserId = Objects.requireNonNull(userId, "User id is required to create a refresh token");
        User user = userRepository.getReferenceById(resolvedUserId);
        Instant now = Instant.now();
    refreshTokenRepository.deleteByUserAndExpiresAtBefore(user, now);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUser(user);
        refreshToken.setCreatedAt(now);
        refreshToken.setExpiresAt(now.plusSeconds(jwtProperties.refreshTokenTtlSeconds()));
        refreshToken.setRevoked(false);
        refreshTokenRepository.save(refreshToken);

        return refreshToken.getToken();
    }

    private void validateRegistration(RegisterRequest request) {
        String normalizedUsername = StringNormalizer.normalizeUsername(request.username());
        String normalizedEmail = StringNormalizer.normalizeEmail(request.email());

        if (userRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
            throw new ConflictException("Username already in use");
        }
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ConflictException("Email already in use");
        }
    }
}
