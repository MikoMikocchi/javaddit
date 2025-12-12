package com.example.javaddit.features.comment.repository;

import com.example.javaddit.features.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId ORDER BY c.createdAt ASC")
    List<Comment> findByPostIdOrderByCreatedAtAsc(@Param("postId") Long postId);

    @Query("SELECT c.score FROM Comment c WHERE c.id = :commentId")
    Integer findScoreById(@Param("commentId") Long commentId);
}
