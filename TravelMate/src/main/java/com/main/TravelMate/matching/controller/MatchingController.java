package com.main.TravelMate.matching.controller;

import com.main.TravelMate.common.security.CustomUserDetails;
import com.main.TravelMate.matching.dto.MatchingRequestDTO;
import com.main.TravelMate.matching.entity.MatchingRequest;
import com.main.TravelMate.matching.service.MatchingService;
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

    @PostMapping("/request")
    public ResponseEntity<String> request(@RequestBody MatchingRequestDTO dto,
                                          Authentication authentication) {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        matchingService.sendRequest(user.getUsername(), dto);
        return ResponseEntity.ok("매칭 요청 완료");
    }

    @PostMapping("/accept/{matchId}")
    public ResponseEntity<String> accept(@PathVariable Long matchId,
                                         Authentication authentication) {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        matchingService.acceptRequest(matchId, user.getUsername());
        return ResponseEntity.ok("매칭 수락 완료");
    }

    @GetMapping
    public ResponseEntity<List<MatchingRequest>> getMyRequests(Authentication authentication) {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(matchingService.getMyMatchingList(user.getUsername()));
    }
}
