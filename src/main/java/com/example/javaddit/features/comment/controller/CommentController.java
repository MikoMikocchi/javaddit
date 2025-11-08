package com.example.javaddit.features.comment.controller;

import com.example.javaddit.features.comment.dto.CommentRequest;
import com.example.javaddit.features.comment.dto.CommentResponse;
import com.example.javaddit.features.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long postId) {
        List<CommentResponse> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest request
    ) {
        CommentResponse created = commentService.createComment(postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
