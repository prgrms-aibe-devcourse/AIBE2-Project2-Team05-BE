package com.main.TravelMate.feed.controller;

import com.main.TravelMate.common.security.CustomUserDetails;
import com.main.TravelMate.feed.dto.FeedCreateRequestDto;
import com.main.TravelMate.feed.dto.FeedResponseDto;
import com.main.TravelMate.feed.service.TravelFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class TravelFeedController {

    private final TravelFeedService feedService;

    @PostMapping
    public ResponseEntity<String> createFeed(@RequestBody FeedCreateRequestDto request,
                                             Authentication authentication) {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        feedService.createFeed(request, user.getUsername());
        return ResponseEntity.ok("피드 등록 완료");
    }

    @GetMapping
    public ResponseEntity<List<FeedResponseDto>> getAllFeeds() {
        return ResponseEntity.ok(feedService.getAllFeeds());
    }
}
