package com.example.javaddit.features.vote.dto;

import com.example.javaddit.features.vote.entity.VoteType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for vote responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteResponse {

    private String message;
    private Integer newScore;
    private VoteType userVote;  // null if vote was removed
}
