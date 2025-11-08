package com.example.javaddit.features.user.service;

import com.example.javaddit.features.user.dto.UserResponse;
import com.example.javaddit.features.user.entity.User;
import com.example.javaddit.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        // Don't return deleted users
        if (user.getIsDeleted()) {
            throw new IllegalArgumentException("User not found: " + username);
        }

        return mapToResponse(user);
    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setDisplayName(user.getDisplayName());
        response.setAbout(user.getAbout());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
