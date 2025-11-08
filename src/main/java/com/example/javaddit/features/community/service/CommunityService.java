package com.example.javaddit.features.community.service;

import com.example.javaddit.features.community.dto.CommunityRequest;
import com.example.javaddit.features.community.dto.CommunityResponse;
import com.example.javaddit.features.community.entity.Community;
import com.example.javaddit.features.community.repository.CommunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityRepository communityRepository;

    @Transactional(readOnly = true)
    public List<CommunityResponse> getAllCommunities() {
        return communityRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CommunityResponse getCommunityByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Community name cannot be null or empty");
        }

        Community community = communityRepository.findByName(name.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Community not found: " + name));

        return mapToResponse(community);
    }

    @Transactional
    public CommunityResponse createCommunity(CommunityRequest request) {
        if (communityRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Community with name '" + request.getName() + "' already exists");
        }

        Community community = new Community();
        community.setName(request.getName().toLowerCase());
        community.setTitle(request.getTitle());
        community.setDescription(request.getDescription());
        community.setIsNsfw(Boolean.TRUE.equals(request.getIsNsfw()));

        Community saved = communityRepository.save(community);
        return mapToResponse(saved);
    }

    private CommunityResponse mapToResponse(Community community) {
        CommunityResponse response = new CommunityResponse();
        response.setId(community.getId());
        response.setName(community.getName());
        response.setTitle(community.getTitle());
        response.setDescription(community.getDescription());
        response.setIsNsfw(community.getIsNsfw());
        response.setCreatedAt(community.getCreatedAt());
        response.setUpdatedAt(community.getUpdatedAt());
        return response;
    }
}
