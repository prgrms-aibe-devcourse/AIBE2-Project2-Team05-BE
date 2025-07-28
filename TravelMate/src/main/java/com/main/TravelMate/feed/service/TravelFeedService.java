package com.main.TravelMate.feed.service;

import com.main.TravelMate.feed.dto.FeedCreateRequestDto;
import com.main.TravelMate.feed.dto.FeedResponseDto;
import com.main.TravelMate.feed.entity.TravelFeed;
import com.main.TravelMate.feed.repository.TravelFeedRepository;
import com.main.TravelMate.plan.entity.TravelPlan;
import com.main.TravelMate.plan.repository.TravelPlanRepository;
import com.main.TravelMate.user.entity.User;
import com.main.TravelMate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TravelFeedService {

    private final TravelFeedRepository feedRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final UserRepository userRepository;

    public void createFeed(FeedCreateRequestDto request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("유저 없음"));

        TravelPlan plan = travelPlanRepository.findById(request.getTravelPlanId())
                .orElseThrow(() -> new IllegalArgumentException("일정 없음"));

        // 이미 피드 등록된 일정은 차단 (unique)
        if (feedRepository.findAll().stream()
                .anyMatch(f -> f.getTravelPlan().getId().equals(plan.getId()))) {
            throw new IllegalStateException("해당 여행계획에 이미 피드가 존재합니다.");
        }

        TravelFeed feed = TravelFeed.builder()
                .user(user)
                .travelPlan(plan)
                .imageUrl(request.getImageUrl())
                .caption(request.getCaption())
                .build();

        feedRepository.save(feed);
    }

    public List<FeedResponseDto> getAllFeeds() {
        return feedRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(feed -> new FeedResponseDto(
                        feed.getId(),
                        feed.getUser().getEmail(),
                        feed.getImageUrl(),
                        feed.getCaption(),
                        feed.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
}
