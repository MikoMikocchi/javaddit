package com.example.javaddit.features.user.controller;

import com.example.javaddit.features.user.dto.UserEmailUpdateRequest;
import com.example.javaddit.features.user.dto.UserPasswordUpdateRequest;
import com.example.javaddit.features.user.dto.UserProfileUpdateRequest;
import com.example.javaddit.features.user.dto.UserResponse;
import com.example.javaddit.features.user.service.UserService;
import com.example.javaddit.core.exception.AuthenticationException;
import com.example.javaddit.core.exception.ValidationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        UserResponse user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me/profile")
    public ResponseEntity<UserResponse> updateProfile(@AuthenticationPrincipal Principal principal,
                                                      @Valid @RequestBody UserProfileUpdateRequest request) {
        long userId = resolveAuthenticatedUserId(principal);
        UserResponse updatedUser = userService.updateProfile(userId, request);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/me/email")
    public ResponseEntity<UserResponse> updateEmail(@AuthenticationPrincipal Principal principal,
                                                    @Valid @RequestBody UserEmailUpdateRequest request) {
        long userId = resolveAuthenticatedUserId(principal);
        UserResponse updatedUser = userService.updateEmail(userId, request);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> updatePassword(@AuthenticationPrincipal Principal principal,
                                               @Valid @RequestBody UserPasswordUpdateRequest request) {
        long userId = resolveAuthenticatedUserId(principal);
        userService.updatePassword(userId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/subscribe/{subscribedToId}")
    public ResponseEntity<UserResponse> subscribe(@AuthenticationPrincipal Principal principal,
                                                  @PathVariable long subscribedToId) {
        long subscriberId = resolveAuthenticatedUserId(principal);
        UserResponse updatedUser = userService.subscribe(subscriberId, subscribedToId);
        return ResponseEntity.status(HttpStatus.CREATED).body(updatedUser);
    }

    @DeleteMapping("/me/unsubscribe/{subscribedToId}")
    public ResponseEntity<UserResponse> unsubscribe(@AuthenticationPrincipal Principal principal,
                                                    @PathVariable long subscribedToId) {
        long subscriberId = resolveAuthenticatedUserId(principal);
        UserResponse updatedUser = userService.unsubscribe(subscriberId, subscribedToId);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/me/block/{blockedId}")
    public ResponseEntity<UserResponse> blockUser(@AuthenticationPrincipal Principal principal,
                                                  @PathVariable long blockedId) {
        long blockerId = resolveAuthenticatedUserId(principal);
        UserResponse updatedUser = userService.blockUser(blockerId, blockedId);
        return ResponseEntity.status(HttpStatus.CREATED).body(updatedUser);
    }

    @DeleteMapping("/me/unblock/{blockedId}")
    public ResponseEntity<UserResponse> unblockUser(@AuthenticationPrincipal Principal principal,
                                                    @PathVariable long blockedId) {
        long blockerId = resolveAuthenticatedUserId(principal);
        UserResponse updatedUser = userService.unblockUser(blockerId, blockedId);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Safely resolves the authenticated user's numeric ID from Principal.
     * If the principal name is not numeric, falls back to resolving by username.
     * Throws AuthenticationException if principal is null or name empty.
     */
    private long resolveAuthenticatedUserId(Principal principal) {
        if (principal == null) {
            throw new AuthenticationException("Unauthenticated: principal is null");
        }
        String name = principal.getName();
        if (name == null || name.isBlank()) {
            throw new ValidationException("Authenticated principal name is empty");
        }
        try {
            return Long.parseLong(name);
        } catch (NumberFormatException ex) {
            // Fallback: treat principal name as username
            UserResponse user = userService.getUserByUsername(name);
            if (user.getId() == null) {
                throw new AuthenticationException("Resolved user has null ID for principal name: " + name);
            }
            return user.getId();
        }
    }
}
