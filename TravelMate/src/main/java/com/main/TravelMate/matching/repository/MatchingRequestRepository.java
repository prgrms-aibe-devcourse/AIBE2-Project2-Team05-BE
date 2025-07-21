package com.main.TravelMate.matching.repository;

import com.main.TravelMate.matching.entity.MatchingRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchingRequestRepository extends JpaRepository<MatchingRequest, Long> {
    List<MatchingRequest> findBySenderIdOrReceiverId(Long senderId, Long receiverId);
}
