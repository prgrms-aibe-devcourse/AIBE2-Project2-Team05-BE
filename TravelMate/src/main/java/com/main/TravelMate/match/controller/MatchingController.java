package com.main.TravelMate.match.controller;

import com.main.TravelMate.common.security.CustomUserDetails;
import com.main.TravelMate.match.domain.MatchingStatus;
import com.main.TravelMate.match.dto.MatchRecommendationDto;
import com.main.TravelMate.match.dto.MatchRequestDto;
import com.main.TravelMate.match.dto.MatchResponseDto;
import com.main.TravelMate.match.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;

    @GetMapping("/recommendations")
    public ResponseEntity<List<MatchRecommendationDto>> getRecommendations(Authentication auth) {
        Long userId = ((CustomUserDetails) auth.getPrincipal()).getUserId();
        return ResponseEntity.ok(matchingService.getRecommendations(userId));
    }

    @PostMapping("/request")
    public ResponseEntity<MatchResponseDto> sendRequest(
            @RequestBody MatchRequestDto request,
            Authentication auth) {
        Long senderId = ((CustomUserDetails) auth.getPrincipal()).getUserId();
        Long matchId = matchingService.sendRequest(senderId, request);
        return ResponseEntity.ok(new MatchResponseDto(matchId, MatchingStatus.PENDING));
    }

    @PatchMapping("/respond")
    public ResponseEntity<String> respondToRequest(
            @RequestBody MatchResponseDto requestDto
    ) {
        matchingService.respondToRequest(requestDto.getMatchId(), requestDto.getStatus());
        return ResponseEntity.ok("매칭 요청에 응답했습니다: " + requestDto.getStatus());
    }

    @DeleteMapping("/cancel/{matchId}")
    public ResponseEntity<String> cancelRequest(
            @PathVariable Long matchId,
            Authentication auth
    ) {
        Long senderId = ((CustomUserDetails) auth.getPrincipal()).getUserId();
        matchingService.cancelRequest(matchId, senderId);
        return ResponseEntity.ok("매칭 요청이 취소되었습니다.");
    }

    @PostMapping("/reject")
    public ResponseEntity<Void> rejectPlan(
            @RequestParam Long senderId,
            @RequestParam Long planId) {
        matchingService.rejectPlan(senderId, planId);
        return ResponseEntity.ok().build();
    }
}
