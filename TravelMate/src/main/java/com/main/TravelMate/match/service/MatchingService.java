package com.main.TravelMate.match.service;

import com.main.TravelMate.match.domain.MatchingStatus;
import com.main.TravelMate.match.dto.MatchRecommendationDto;
import com.main.TravelMate.match.dto.MatchRequestDto;

import java.util.List;

public interface MatchingService {
    List<MatchRecommendationDto> getRecommendations(Long userId);
    Long sendRequest(Long senderId, MatchRequestDto request);
    void respondToRequest(Long matchId, MatchingStatus status);
}