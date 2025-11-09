package com.example.javaddit.features.user.service;

import com.example.javaddit.core.exception.AuthenticationException;
import com.example.javaddit.core.exception.ConflictException;
import com.example.javaddit.core.exception.NotFoundException;
import com.example.javaddit.core.exception.ValidationException;
import com.example.javaddit.features.user.dto.UserEmailUpdateRequest;
import com.example.javaddit.features.user.dto.UserPasswordUpdateRequest;
import com.example.javaddit.features.user.dto.UserProfileUpdateRequest;
import com.example.javaddit.features.user.dto.UserResponse;
import com.example.javaddit.features.user.entity.User;
import com.example.javaddit.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username cannot be null or empty");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));

        if (user.getIsDeleted()) {
            throw new NotFoundException("User not found: " + username);
        }

        return mapToResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(long userId, UserProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }
        if (request.getAbout() != null) {
            user.setAbout(request.getAbout());
        }
        if (request.getProfilePictureUrl() != null) {
            user.setProfilePictureUrl(request.getProfilePictureUrl());
        }
        if (request.getIsPrivate() != null) {
            user.setIsPrivate(request.getIsPrivate());
        }
        if (request.getReceiveNotifications() != null) {
            user.setReceiveNotifications(request.getReceiveNotifications());
        }

        return saveAndMap(user);
    }

    @Transactional
    public UserResponse updateEmail(long userId, UserEmailUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("Email already in use: " + request.getEmail());
        }

        user.setEmail(request.getEmail());
        return saveAndMap(user);
    }

    @Transactional
    public void updatePassword(long userId, UserPasswordUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new AuthenticationException("Invalid current password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user); // void method; caller doesn't need response
    }

    @Transactional
    public UserResponse subscribe(long subscriberId, long subscribedToId) {
        User subscriber = userRepository.findById(subscriberId)
                .orElseThrow(() -> new NotFoundException("Subscriber not found: " + subscriberId));
        User subscribedTo = userRepository.findById(subscribedToId)
                .orElseThrow(() -> new NotFoundException("User to subscribe to not found: " + subscribedToId));

        if (subscriber.equals(subscribedTo)) {
            throw new ValidationException("Cannot subscribe to yourself");
        }

        subscriber.getSubscriptions().add(subscribedTo);
        return saveAndMap(subscriber);
    }

    @Transactional
    public UserResponse unsubscribe(long subscriberId, long subscribedToId) {
        User subscriber = userRepository.findById(subscriberId)
                .orElseThrow(() -> new NotFoundException("Subscriber not found: " + subscriberId));
        User subscribedTo = userRepository.findById(subscribedToId)
                .orElseThrow(() -> new NotFoundException("User to unsubscribe from not found: " + subscribedToId));

        subscriber.getSubscriptions().remove(subscribedTo);
        return saveAndMap(subscriber);
    }

    @Transactional
    public UserResponse blockUser(long blockerId, long blockedId) {
        User blocker = userRepository.findById(blockerId)
                .orElseThrow(() -> new NotFoundException("Blocker not found: " + blockerId));
        User blocked = userRepository.findById(blockedId)
                .orElseThrow(() -> new NotFoundException("User to block not found: " + blockedId));

        if (blocker.equals(blocked)) {
            throw new ValidationException("Cannot block yourself");
        }

        blocker.getBlockedUsers().add(blocked);
        return saveAndMap(blocker);
    }

    @Transactional
    public UserResponse unblockUser(long blockerId, long blockedId) {
        User blocker = userRepository.findById(blockerId)
                .orElseThrow(() -> new NotFoundException("Blocker not found: " + blockerId));
        User blocked = userRepository.findById(blockedId)
                .orElseThrow(() -> new NotFoundException("User to unblock not found: " + blockedId));

        blocker.getBlockedUsers().remove(blocked);
        return saveAndMap(blocker);
    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setDisplayName(user.getDisplayName());
        response.setAbout(user.getAbout());
        response.setProfilePictureUrl(user.getProfilePictureUrl());
        response.setIsPrivate(user.getIsPrivate());
        response.setReceiveNotifications(user.getReceiveNotifications());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    @SuppressWarnings("null")
    private UserResponse saveAndMap(User user) {
        User persisted = userRepository.save(user); // assumed non-null by repository contract
        if (persisted.getId() == null) {
            throw new ValidationException("Persisted user has null id");
        }
        return mapToResponse(persisted);
    }
}
