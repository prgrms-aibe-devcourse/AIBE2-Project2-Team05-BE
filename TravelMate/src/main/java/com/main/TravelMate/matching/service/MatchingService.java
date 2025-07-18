package com.main.TravelMate.matching.service;

import com.main.TravelMate.matching.domain.MatchingStatus;
import com.main.TravelMate.matching.dto.MatchingRequestDTO;
import com.main.TravelMate.matching.entity.MatchingRequest;
import com.main.TravelMate.matching.repository.MatchingRequestRepository;
import com.main.TravelMate.plan.entity.TravelPlan;
import com.main.TravelMate.plan.repository.TravelPlanRepository;
import com.main.TravelMate.user.entity.User;
import com.main.TravelMate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final MatchingRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final TravelPlanRepository planRepository;

    public void sendRequest(String senderEmail, MatchingRequestDTO dto) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new UsernameNotFoundException("보내는 사용자 없음"));

        User receiver = userRepository.findById(dto.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException("받는 사용자 없음"));

        TravelPlan plan = planRepository.findById(dto.getPlanId())
                .orElseThrow(() -> new IllegalArgumentException("플랜 없음"));

        MatchingRequest request = MatchingRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .plan(plan)
                .status(MatchingStatus.PENDING)
                .build();

        requestRepository.save(request);
    }

    public void acceptRequest(Long matchId, String receiverEmail) {
        MatchingRequest request = requestRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("매칭 요청 없음"));

        if (!request.getReceiver().getEmail().equals(receiverEmail)) {
            throw new AccessDeniedException("수락 권한 없음");
        }

        request.setStatus(MatchingStatus.ACCEPTED);
    }

    public List<MatchingRequest> getMyMatchingList(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자 없음"));

        return requestRepository.findBySenderIdOrReceiverId(user.getId(), user.getId());
    }
}
