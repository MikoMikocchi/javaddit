package com.example.javaddit.features.user.controller;

import com.example.javaddit.features.user.dto.UserEmailUpdateRequest;
import com.example.javaddit.features.user.dto.UserPasswordUpdateRequest;
import com.example.javaddit.features.user.dto.UserProfileUpdateRequest;
import com.example.javaddit.features.user.dto.UserResponse;
import com.example.javaddit.features.user.service.UserService;
import com.example.javaddit.core.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


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
    public ResponseEntity<UserResponse> updateProfile(@AuthenticationPrincipal UserPrincipal principal,
                                                      @Valid @RequestBody UserProfileUpdateRequest request) {
        long userId = principal.getId();
        UserResponse updatedUser = userService.updateProfile(userId, request);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/me/email")
    public ResponseEntity<UserResponse> updateEmail(@AuthenticationPrincipal UserPrincipal principal,
                                                    @Valid @RequestBody UserEmailUpdateRequest request) {
        long userId = principal.getId();
        UserResponse updatedUser = userService.updateEmail(userId, request);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> updatePassword(@AuthenticationPrincipal UserPrincipal principal,
                                               @Valid @RequestBody UserPasswordUpdateRequest request) {
        long userId = principal.getId();
        userService.updatePassword(userId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/subscribe/{subscribedToId}")
    public ResponseEntity<UserResponse> subscribe(@AuthenticationPrincipal UserPrincipal principal,
                                                  @PathVariable long subscribedToId) {
        long subscriberId = principal.getId();
        UserResponse updatedUser = userService.subscribe(subscriberId, subscribedToId);
        return ResponseEntity.status(HttpStatus.CREATED).body(updatedUser);
    }

    @DeleteMapping("/me/unsubscribe/{subscribedToId}")
    public ResponseEntity<UserResponse> unsubscribe(@AuthenticationPrincipal UserPrincipal principal,
                                                    @PathVariable long subscribedToId) {
        long subscriberId = principal.getId();
        UserResponse updatedUser = userService.unsubscribe(subscriberId, subscribedToId);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/me/block/{blockedId}")
    public ResponseEntity<UserResponse> blockUser(@AuthenticationPrincipal UserPrincipal principal,
                                                  @PathVariable long blockedId) {
        long blockerId = principal.getId();
        UserResponse updatedUser = userService.blockUser(blockerId, blockedId);
        return ResponseEntity.status(HttpStatus.CREATED).body(updatedUser);
    }

    @DeleteMapping("/me/unblock/{blockedId}")
    public ResponseEntity<UserResponse> unblockUser(@AuthenticationPrincipal UserPrincipal principal,
                                                    @PathVariable long blockedId) {
        long blockerId = principal.getId();
        UserResponse updatedUser = userService.unblockUser(blockerId, blockedId);
        return ResponseEntity.ok(updatedUser);
    }
}
