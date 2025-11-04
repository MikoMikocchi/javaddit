package com.example.javaddit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommunityResponse {

    private Long id;
    private String name;
    private String title;
    private String description;
    private Boolean isNsfw;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
