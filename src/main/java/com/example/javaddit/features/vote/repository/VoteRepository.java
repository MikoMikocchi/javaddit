package com.example.javaddit.features.vote.repository;

import com.example.javaddit.features.vote.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Vote entity operations.
 */
@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    /**
     * Find a vote by user ID and post ID.
     */
    Optional<Vote> findByUserIdAndPostId(Long userId, Long postId);

    /**
     * Find a vote by user ID and comment ID.
     */
    Optional<Vote> findByUserIdAndCommentId(Long userId, Long commentId);

    /**
     * Delete a vote by user ID and post ID.
     */
    void deleteByUserIdAndPostId(Long userId, Long postId);

    /**
     * Delete a vote by user ID and comment ID.
     */
    void deleteByUserIdAndCommentId(Long userId, Long commentId);
}
