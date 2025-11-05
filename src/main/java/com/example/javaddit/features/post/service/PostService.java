package com.example.javaddit.features.post.service;

import com.example.javaddit.features.post.dto.PostRequest;
import com.example.javaddit.features.post.dto.PostResponse;
import com.example.javaddit.features.community.entity.Community;
import com.example.javaddit.features.post.entity.Post;
import com.example.javaddit.features.user.entity.User;
import com.example.javaddit.features.community.repository.CommunityRepository;
import com.example.javaddit.features.post.repository.PostRepository;
import com.example.javaddit.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<PostResponse> getPosts(String communityName) {
        List<Post> posts;

        if (communityName != null && !communityName.isEmpty()) {
            posts = postRepository.findByCommunityName(communityName);
        } else {
            posts = postRepository.findAllOrderByCreatedAtDesc();
        }

        return posts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PostResponse createPost(PostRequest request) {
        // Validate content or url (XOR)
        boolean hasContent = request.getContent() != null && !request.getContent().trim().isEmpty();
        boolean hasUrl = request.getUrl() != null && !request.getUrl().trim().isEmpty();

        if (hasContent == hasUrl) {
            throw new IllegalArgumentException("Post must have either content or url, but not both");
        }

        // Find community
        Community community = communityRepository.findByName(request.getCommunityName())
                .orElseThrow(() -> new IllegalArgumentException("Community not found: " + request.getCommunityName()));

        // Get default user (in real app, this would be the authenticated user)
        User author = userRepository.findByUsername("default_user")
                .orElseThrow(() -> new IllegalStateException("Default user not found"));

        // Create post
        Post post = new Post();
        post.setCommunity(community);
        post.setAuthor(author);
        post.setTitle(request.getTitle());
        post.setSlug(generateSlug(request.getTitle()));
        post.setContent(hasContent ? request.getContent() : null);
        post.setUrl(hasUrl ? request.getUrl() : null);

        Post saved = postRepository.save(post);
        return mapToResponse(saved);
    }

    private String generateSlug(String title) {
        String normalized = Normalizer.normalize(title, Normalizer.Form.NFD);
        String slug = normalized.replaceAll("[^\\p{ASCII}]", "")
                .toLowerCase(Locale.ENGLISH)
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");

        return slug.isEmpty() ? "post" : slug;
    }

    private PostResponse mapToResponse(Post post) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setCommunityName(post.getCommunity().getName());
        response.setTitle(post.getTitle());
        response.setSlug(post.getSlug());
        response.setContent(post.getContent());
        response.setUrl(post.getUrl());
        response.setScore(post.getScore());
        response.setCommentsCount(post.getCommentsCount());
        response.setCreatedAt(post.getCreatedAt());
        response.setUpdatedAt(post.getUpdatedAt());
        return response;
    }
}
