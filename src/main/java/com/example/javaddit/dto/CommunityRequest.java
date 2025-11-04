package com.example.javaddit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommunityRequest {

    @NotBlank(message = "Community name is required")
    @Size(min = 3, max = 50, message = "Community name must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-z0-9_][a-z0-9_-]{2,49}$", message = "Community name must be lowercase and contain only letters, numbers, underscores, and hyphens")
    private String name;

    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    private String description;

    private Boolean isNsfw = false;
}
