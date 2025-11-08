package com.example.javaddit.features.vote.dto;

import com.example.javaddit.features.vote.entity.VoteType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for vote requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteRequest {

    @NotNull(message = "Vote type is required")
    private VoteType voteType;
}
