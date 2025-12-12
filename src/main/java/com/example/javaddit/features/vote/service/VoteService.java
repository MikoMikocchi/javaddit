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
     * Vote on a post. If the user already voted with the same type, remove the vote
     * (toggle).
     * If the user voted with a different type, update the vote.
     * If the user hasn't voted yet, create a new vote.
     */
    @Transactional
    public VoteResponse voteOnPost(Long userId, Long postId, VoteType voteType) {
        if (postId == null) {
            throw new ValidationException("Post ID cannot be null");
        }
        if (userId == null) {
            throw new ValidationException("User ID cannot be null");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found: " + postId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        Optional<Vote> existingVote = voteRepository.findByUserIdAndPostId(userId, postId);

        VoteType resultVoteType;
        String message;

        if (existingVote.isPresent()) {
            Vote vote = existingVote.get();

            if (vote.getVoteType() == voteType) {
                voteRepository.delete(vote);
                resultVoteType = null;
                message = "Vote removed";
            } else {
                vote.setVoteType(voteType);
                voteRepository.save(vote);
                resultVoteType = voteType;
                message = "Vote updated";
            }
        } else {
            Vote newVote = new Vote();
            newVote.setUser(user);
            newVote.setPost(post);
            newVote.setVoteType(voteType);
            voteRepository.save(newVote);
            resultVoteType = voteType;
            message = "Vote recorded";
        }

        voteRepository.flush();
        Integer score = postRepository.findScoreById(postId);
        if (score == null)
            score = 0;

        return new VoteResponse(message, score, resultVoteType);
    }

    /**
     * Vote on a comment. If the user already voted with the same type, remove the
     * vote (toggle).
     * If the user voted with a different type, update the vote.
     * If the user hasn't voted yet, create a new vote.
     */
    @Transactional
    public VoteResponse voteOnComment(Long userId, Long commentId, VoteType voteType) {
        if (commentId == null) {
            throw new ValidationException("Comment ID cannot be null");
        }
        if (userId == null) {
            throw new ValidationException("User ID cannot be null");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found: " + commentId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        Optional<Vote> existingVote = voteRepository.findByUserIdAndCommentId(userId, commentId);

        VoteType resultVoteType;
        String message;

        if (existingVote.isPresent()) {
            Vote vote = existingVote.get();

            if (vote.getVoteType() == voteType) {
                voteRepository.delete(vote);
                resultVoteType = null;
                message = "Vote removed";
            } else {
                vote.setVoteType(voteType);
                voteRepository.save(vote);
                resultVoteType = voteType;
                message = "Vote updated";
            }
        } else {
            Vote newVote = new Vote();
            newVote.setUser(user);
            newVote.setComment(comment);
            newVote.setVoteType(voteType);
            voteRepository.save(newVote);
            resultVoteType = voteType;
            message = "Vote recorded";
        }

        voteRepository.flush();
        Integer score = commentRepository.findScoreById(commentId);
        if (score == null)
            score = 0;

        return new VoteResponse(message, score, resultVoteType);
    }

    /**
     * Remove a vote from a post.
     */
    @Transactional
    public VoteResponse removeVoteFromPost(Long userId, Long postId) {
        if (postId == null) {
            throw new ValidationException("Post ID cannot be null");
        }
        if (userId == null) {
            throw new ValidationException("User ID cannot be null");
        }

        postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found: " + postId));

        Vote vote = voteRepository.findByUserIdAndPostId(userId, postId)
                .orElseThrow(() -> new NotFoundException("Vote not found"));

        voteRepository.delete(Objects.requireNonNull(vote));
        voteRepository.flush();
        Integer score = postRepository.findScoreById(postId);
        if (score == null)
            score = 0;

        return new VoteResponse("Vote removed", score, null);
    }

    /**
     * Remove a vote from a comment.
     */
    @Transactional
    public VoteResponse removeVoteFromComment(Long userId, Long commentId) {
        if (commentId == null) {
            throw new ValidationException("Comment ID cannot be null");
        }
        if (userId == null) {
            throw new ValidationException("User ID cannot be null");
        }

        commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found: " + commentId));

        Vote vote = voteRepository.findByUserIdAndCommentId(userId, commentId)
                .orElseThrow(() -> new NotFoundException("Vote not found"));

        voteRepository.delete(Objects.requireNonNull(vote));
        voteRepository.flush();
        Integer score = commentRepository.findScoreById(commentId);
        if (score == null)
            score = 0;

        return new VoteResponse("Vote removed", score, null);
    }
}
