package com.example.javaddit.features.vote.service;

import com.example.javaddit.core.exception.NotFoundException;
import com.example.javaddit.core.exception.ValidationException;
import com.example.javaddit.features.comment.entity.Comment;
import com.example.javaddit.features.comment.repository.CommentRepository;
import com.example.javaddit.features.post.entity.Post;
import com.example.javaddit.features.post.repository.PostRepository;
import com.example.javaddit.features.user.entity.User;
import com.example.javaddit.features.user.repository.UserRepository;
import com.example.javaddit.features.vote.dto.VoteResponse;
import com.example.javaddit.features.vote.entity.Vote;
import com.example.javaddit.features.vote.entity.VoteType;
import com.example.javaddit.features.vote.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

/**
 * Service for handling vote operations on posts and comments.
 */
@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    /**
     * Vote on a post. If the user already voted with the same type, remove the vote (toggle).
     * If the user voted with a different type, update the vote.
     * If the user hasn't voted yet, create a new vote.
     */
    @Transactional
    public VoteResponse voteOnPost(Long userId, Long postId, VoteType voteType) {
        // Validate parameters
        if (postId == null) {
            throw new ValidationException("Post ID cannot be null");
        }
        if (userId == null) {
            throw new ValidationException("User ID cannot be null");
        }

        // Validate post exists
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found: " + postId));

        // Validate user exists (in real app, this would be the authenticated user)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        // Check if user already voted
        Optional<Vote> existingVote = voteRepository.findByUserIdAndPostId(userId, postId);

        VoteType resultVoteType;
        String message;

        if (existingVote.isPresent()) {
            Vote vote = existingVote.get();

            if (vote.getVoteType() == voteType) {
                // Same vote type - remove vote (toggle)
                voteRepository.delete(vote);
                resultVoteType = null;
                message = "Vote removed";
            } else {
                // Different vote type - update vote
                vote.setVoteType(voteType);
                voteRepository.save(vote);
                resultVoteType = voteType;
                message = "Vote updated";
            }
        } else {
            // No existing vote - create new vote
            Vote newVote = new Vote();
            newVote.setUser(user);
            newVote.setPost(post);
            newVote.setVoteType(voteType);
            voteRepository.save(newVote);
            resultVoteType = voteType;
            message = "Vote recorded";
        }

        // Refresh post to get updated score (calculated by database triggers)
        Post updatedPost = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalStateException("Post not found after voting"));

        Integer score = updatedPost.getScore();
        if (score == null) {
            score = 0;
        }

        return new VoteResponse(message, score, resultVoteType);
    }

    /**
     * Vote on a comment. If the user already voted with the same type, remove the vote (toggle).
     * If the user voted with a different type, update the vote.
     * If the user hasn't voted yet, create a new vote.
     */
    @Transactional
    public VoteResponse voteOnComment(Long userId, Long commentId, VoteType voteType) {
        // Validate parameters
        if (commentId == null) {
            throw new ValidationException("Comment ID cannot be null");
        }
        if (userId == null) {
            throw new ValidationException("User ID cannot be null");
        }

        // Validate comment exists
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found: " + commentId));

        // Validate user exists (in real app, this would be the authenticated user)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        // Check if user already voted
        Optional<Vote> existingVote = voteRepository.findByUserIdAndCommentId(userId, commentId);

        VoteType resultVoteType;
        String message;

        if (existingVote.isPresent()) {
            Vote vote = existingVote.get();

            if (vote.getVoteType() == voteType) {
                // Same vote type - remove vote (toggle)
                voteRepository.delete(vote);
                resultVoteType = null;
                message = "Vote removed";
            } else {
                // Different vote type - update vote
                vote.setVoteType(voteType);
                voteRepository.save(vote);
                resultVoteType = voteType;
                message = "Vote updated";
            }
        } else {
            // No existing vote - create new vote
            Vote newVote = new Vote();
            newVote.setUser(user);
            newVote.setComment(comment);
            newVote.setVoteType(voteType);
            voteRepository.save(newVote);
            resultVoteType = voteType;
            message = "Vote recorded";
        }

        // Refresh comment to get updated score (calculated by database triggers)
        Comment updatedComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalStateException("Comment not found after voting"));

        Integer score = updatedComment.getScore();
        if (score == null) {
            score = 0;
        }

        return new VoteResponse(message, score, resultVoteType);
    }

    /**
     * Remove a vote from a post.
     */
    @Transactional
    public VoteResponse removeVoteFromPost(Long userId, Long postId) {
        // Validate parameters
        if (postId == null) {
            throw new ValidationException("Post ID cannot be null");
        }
        if (userId == null) {
            throw new ValidationException("User ID cannot be null");
        }

        // Validate post exists
        postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found: " + postId));

        // Find and delete vote
        Vote vote = voteRepository.findByUserIdAndPostId(userId, postId)
                .orElseThrow(() -> new NotFoundException("Vote not found"));

        voteRepository.delete(Objects.requireNonNull(vote));

        // Refresh post to get updated score
        Post updatedPost = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalStateException("Post not found after removing vote"));

        Integer score = updatedPost.getScore();
        if (score == null) {
            score = 0;
        }

        return new VoteResponse("Vote removed", score, null);
    }

    /**
     * Remove a vote from a comment.
     */
    @Transactional
    public VoteResponse removeVoteFromComment(Long userId, Long commentId) {
        // Validate parameters
        if (commentId == null) {
            throw new ValidationException("Comment ID cannot be null");
        }
        if (userId == null) {
            throw new ValidationException("User ID cannot be null");
        }

        // Validate comment exists
        commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found: " + commentId));

        // Find and delete vote
        Vote vote = voteRepository.findByUserIdAndCommentId(userId, commentId)
                .orElseThrow(() -> new NotFoundException("Vote not found"));

        voteRepository.delete(Objects.requireNonNull(vote));

        // Refresh comment to get updated score
        Comment updatedComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalStateException("Comment not found after removing vote"));

        Integer score = updatedComment.getScore();
        if (score == null) {
            score = 0;
        }

        return new VoteResponse("Vote removed", score, null);
    }
}
