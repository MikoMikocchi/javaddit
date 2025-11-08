package com.example.javaddit.features.vote.controller;

import com.example.javaddit.features.vote.dto.VoteRequest;
import com.example.javaddit.features.vote.dto.VoteResponse;
import com.example.javaddit.features.vote.service.VoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for voting operations.
 */
@RestController
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    // Temporary: In a real application, this would come from authentication context
    private static final Long DEFAULT_USER_ID = 1L;

    /**
     * Vote on a post.
     * If the same vote type is submitted twice, it removes the vote (toggle).
     */
    @PostMapping("/api/posts/{postId}/vote")
    public ResponseEntity<VoteResponse> voteOnPost(
            @PathVariable Long postId,
            @Valid @RequestBody VoteRequest request
    ) {
        VoteResponse response = voteService.voteOnPost(DEFAULT_USER_ID, postId, request.getVoteType());
        return ResponseEntity.ok(response);
    }

    /**
     * Remove vote from a post.
     */
    @DeleteMapping("/api/posts/{postId}/vote")
    public ResponseEntity<VoteResponse> removeVoteFromPost(@PathVariable Long postId) {
        VoteResponse response = voteService.removeVoteFromPost(DEFAULT_USER_ID, postId);
        return ResponseEntity.ok(response);
    }

    /**
     * Vote on a comment.
     * If the same vote type is submitted twice, it removes the vote (toggle).
     */
    @PostMapping("/api/comments/{commentId}/vote")
    public ResponseEntity<VoteResponse> voteOnComment(
            @PathVariable Long commentId,
            @Valid @RequestBody VoteRequest request
    ) {
        VoteResponse response = voteService.voteOnComment(DEFAULT_USER_ID, commentId, request.getVoteType());
        return ResponseEntity.ok(response);
    }

    /**
     * Remove vote from a comment.
     */
    @DeleteMapping("/api/comments/{commentId}/vote")
    public ResponseEntity<VoteResponse> removeVoteFromComment(@PathVariable Long commentId) {
        VoteResponse response = voteService.removeVoteFromComment(DEFAULT_USER_ID, commentId);
        return ResponseEntity.ok(response);
    }
}
