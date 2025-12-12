package com.example.javaddit.features.comment.service;

import com.example.javaddit.core.exception.NotFoundException;
import com.example.javaddit.core.exception.ValidationException;
import com.example.javaddit.features.comment.dto.CommentRequest;
import com.example.javaddit.features.comment.dto.CommentResponse;
import com.example.javaddit.features.comment.entity.Comment;
import com.example.javaddit.features.comment.repository.CommentRepository;
import com.example.javaddit.features.post.entity.Post;
import com.example.javaddit.features.post.repository.PostRepository;
import com.example.javaddit.features.user.entity.User;
import com.example.javaddit.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPostId(Long postId) {
        if (postId == null) {
            throw new ValidationException("Post ID cannot be null");
        }

        postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found: " + postId));

        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);

        return comments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponse createComment(Long authorId, Long postId, CommentRequest request) {
        if (postId == null) {
            throw new ValidationException("Post ID cannot be null");
        }
        if (authorId == null) {
            throw new ValidationException("Author ID cannot be null");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found: " + postId));

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("User not found: " + authorId));

        Comment parent = null;
        Long parentId = request.getParentId();
        if (parentId != null) {
            parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new NotFoundException("Parent comment not found: " + parentId));

            if (!parent.getPost().getId().equals(postId)) {
                throw new ValidationException("Parent comment does not belong to this post");
            }
        }

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setParent(parent);
        comment.setContent(request.getContent());

        Comment saved = commentRepository.save(comment);
        return mapToResponse(saved);
    }

    private CommentResponse mapToResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setPostId(comment.getPost().getId());
        response.setAuthorUsername(comment.getAuthor().getUsername());
        response.setParentId(comment.getParent() != null ? comment.getParent().getId() : null);
        response.setContent(comment.getContent());
        response.setScore(comment.getScore());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        return response;
    }
}
