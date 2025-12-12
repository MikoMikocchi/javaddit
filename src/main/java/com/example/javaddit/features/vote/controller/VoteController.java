package com.example.javaddit.features.vote.controller;

import com.example.javaddit.core.security.UserPrincipal;
import com.example.javaddit.features.vote.dto.VoteRequest;
import com.example.javaddit.features.vote.dto.VoteResponse;
import com.example.javaddit.features.vote.service.VoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for voting operations.
 */
@RestController
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    /**
     * Vote on a post.
     * If the same vote type is submitted twice, it removes the vote (toggle).
     */
    @PostMapping("/api/posts/{postId}/vote")
    public ResponseEntity<VoteResponse> voteOnPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody VoteRequest request) {
        VoteResponse response = voteService.voteOnPost(principal.getId(), postId, request.getVoteType());
        return ResponseEntity.ok(response);
    }

    /**
     * Remove vote from a post.
     */
    @DeleteMapping("/api/posts/{postId}/vote")
    public ResponseEntity<VoteResponse> removeVoteFromPost(@PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal principal) {
        VoteResponse response = voteService.removeVoteFromPost(principal.getId(), postId);
        return ResponseEntity.ok(response);
    }

    /**
     * Vote on a comment.
     * If the same vote type is submitted twice, it removes the vote (toggle).
     */
    @PostMapping("/api/comments/{commentId}/vote")
    public ResponseEntity<VoteResponse> voteOnComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody VoteRequest request) {
        VoteResponse response = voteService.voteOnComment(principal.getId(), commentId, request.getVoteType());
        return ResponseEntity.ok(response);
    }

    /**
     * Remove vote from a comment.
     */
    @DeleteMapping("/api/comments/{commentId}/vote")
    public ResponseEntity<VoteResponse> removeVoteFromComment(@PathVariable Long commentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        VoteResponse response = voteService.removeVoteFromComment(principal.getId(), commentId);
        return ResponseEntity.ok(response);
    }
}
