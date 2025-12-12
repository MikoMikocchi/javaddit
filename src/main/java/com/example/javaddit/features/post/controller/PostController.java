package com.example.javaddit.features.post.controller;

import com.example.javaddit.core.security.UserPrincipal;
import com.example.javaddit.features.post.dto.PostRequest;
import com.example.javaddit.features.post.dto.PostResponse;
import com.example.javaddit.features.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<List<PostResponse>> getPosts(
            @RequestParam(required = false) String community) {
        List<PostResponse> posts = postService.getPosts(community);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long id) {
        PostResponse post = postService.getPostById(id);
        return ResponseEntity.ok(post);
    }

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PostRequest request) {
        PostResponse created = postService.createPost(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
