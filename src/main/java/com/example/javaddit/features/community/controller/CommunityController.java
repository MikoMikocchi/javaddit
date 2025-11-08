package com.example.javaddit.features.community.controller;

import com.example.javaddit.features.community.dto.CommunityRequest;
import com.example.javaddit.features.community.dto.CommunityResponse;
import com.example.javaddit.features.community.service.CommunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @GetMapping
    public ResponseEntity<List<CommunityResponse>> getAllCommunities() {
        List<CommunityResponse> communities = communityService.getAllCommunities();
        return ResponseEntity.ok(communities);
    }

    @GetMapping("/{name}")
    public ResponseEntity<CommunityResponse> getCommunityByName(@PathVariable String name) {
        CommunityResponse community = communityService.getCommunityByName(name);
        return ResponseEntity.ok(community);
    }

    @PostMapping
    public ResponseEntity<CommunityResponse> createCommunity(@Valid @RequestBody CommunityRequest request) {
        CommunityResponse created = communityService.createCommunity(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
