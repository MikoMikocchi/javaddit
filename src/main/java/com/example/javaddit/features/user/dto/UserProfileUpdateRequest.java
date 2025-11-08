package com.example.javaddit.features.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileUpdateRequest {

    @Size(min = 3, max = 50, message = "Display name must be between 3 and 50 characters")
    private String displayName;

    @Size(max = 500, message = "About information cannot exceed 500 characters")
    private String about;

    @Size(max = 2048, message = "Profile picture URL cannot exceed 2048 characters")
    private String profilePictureUrl;

    private Boolean isPrivate;

    private Boolean receiveNotifications;
}
