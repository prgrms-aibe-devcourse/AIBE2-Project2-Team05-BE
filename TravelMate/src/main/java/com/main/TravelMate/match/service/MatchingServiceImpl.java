package com.main.TravelMate.match.service;

import com.main.TravelMate.alarm.domain.Alarm;
import com.main.TravelMate.alarm.service.AlarmService;
import com.main.TravelMate.match.domain.MatchingStatus;
import com.main.TravelMate.match.dto.MatchRecommendationDto;
import com.main.TravelMate.match.dto.MatchRequestDto;
import com.main.TravelMate.match.entity.Matching;
import com.main.TravelMate.match.repository.MatchingRepository;
import com.main.TravelMate.plan.entity.TravelPlan;
import com.main.TravelMate.plan.repository.TravelPlanRepository;
import com.main.TravelMate.profile.entity.Profile;
import com.main.TravelMate.user.entity.User;
import com.main.TravelMate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MatchingServiceImpl implements MatchingService {

    private final UserRepository userRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final MatchingRepository matchingRepository;
    private final AlarmService alarmService;

    @Override
    public List<MatchRecommendationDto> getRecommendations(Long userId) {
        User me = userRepository.findById(userId).orElseThrow();
        Profile myProfile = me.getProfile();
        TravelPlan myPlan = travelPlanRepository.findFirstByUserIdOrderByStartDateDesc(userId)
                .orElseThrow(() -> new RuntimeException("플랜 없음"));

        List<TravelPlan> candidates = travelPlanRepository.findRecruitingPlansExcludingUser(userId);

        // 🔽 현재는 유사도 점수 무시하고 recruiting=true면 모두 반환
        return candidates.stream()
                .map(p -> new MatchRecommendationDto(
                        p.getUser().getId(),
                        p.getUser().getNickname(),
                        p.getLocation(),
                        p.getStartDate(),
                        p.getEndDate(),
                        p.getId(),
                        0
                ))
                .toList();

    /* 🔒 유사도 기반 추천 로직 - 일시 비활성화
    return candidates.stream()
            .filter(p -> p.getLocation().equalsIgnoreCase(myPlan.getLocation()))
            .map(p -> {
                int score = calculateCompatibilityScore(myProfile, myPlan, p);
                if (score >= 60) {
                    return new MatchRecommendationDto(
                            p.getUser().getId(),
                            p.getUser().getNickname(),
                            p.getUser().getProfile().getProfileImage(),
                            p.getUser().getProfile().getTravelStyle(),
                            p.getLocation(),
                            p.getStartDate(),
                            p.getEndDate(),
                            p.getId(),
                            score
                    );
                }
                return null;
            })
            .filter(Objects::nonNull)
            .sorted(Comparator.comparingInt(MatchRecommendationDto::getCompatibilityScore).reversed())
            .toList();
    */
    }

    private int calculateCompatibilityScore(TravelPlan myPlan, TravelPlan target) {
        int score = 0;

        // 목적지 매칭 (50점)
        if (target.getLocation().equalsIgnoreCase(myPlan.getLocation())) {
            score += 50;
        }

        // 일정 겹침 (30점)
        int overlap = calculateOverlappingDays(
                myPlan.getStartDate(), myPlan.getEndDate(),
                target.getStartDate(), target.getEndDate());
        if (overlap > 0) {
            long myTotal = ChronoUnit.DAYS.between(myPlan.getStartDate(), myPlan.getEndDate()) + 1;
            double ratio = (double) overlap / myTotal;
            score += (int) (ratio * 30);
        }

        // 그룹 크기 (20점)
        int diff = Math.abs(myPlan.getNumberOfPeople() - target.getNumberOfPeople());
        if (diff == 0) score += 20;
        else if (diff <= 2) score += 10;

        return Math.min(score, 100);
    }
    private int calculateOverlappingDays(LocalDate aStart, LocalDate aEnd, LocalDate bStart, LocalDate bEnd) {
        LocalDate overlapStart = aStart.isAfter(bStart) ? aStart : bStart;
        LocalDate overlapEnd = aEnd.isBefore(bEnd) ? aEnd : bEnd;
        if (overlapStart.isAfter(overlapEnd)) return 0;
        return (int) ChronoUnit.DAYS.between(overlapStart, overlapEnd) + 1;
    }




    @Override
    public Long sendRequest(Long senderId, MatchRequestDto request) {
        User sender = userRepository.findById(senderId).orElseThrow();
        User receiver = userRepository.findById(request.getReceiverId()).orElseThrow();
        TravelPlan plan = travelPlanRepository.findById(request.getPlanId()).orElseThrow();

        if (matchingRepository.existsBySenderIdAndReceiverIdAndPlanId(senderId, receiver.getId(), plan.getId())) {
            throw new IllegalStateException("이미 요청한 사용자입니다.");
        }

        Matching matching = Matching.builder()
                .sender(sender)
                .receiver(receiver)
                .plan(plan)
                .status(MatchingStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        matchingRepository.save(matching);

        // 🔔 알림 전송 (receiver에게)
        alarmService.sendAlarm(
                receiver.getId(),
                sender.getNickname(),
                Alarm.AlarmType.MATCH_REQUEST,
                sender.getNickname() + " 님이 매칭 요청을 보냈습니다."
        );

        return matching.getId();
    }

    @Override
    public void respondToRequest(Long matchId, MatchingStatus status) {
        Matching match = matchingRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("매칭 요청 없음"));

        if (match.getStatus() != MatchingStatus.PENDING) {
            throw new IllegalStateException("이미 응답 처리된 요청입니다.");
        }

        match.updateStatus(status);

        if (status == MatchingStatus.ACCEPTED) {
            // 🔔 알림 전송 (sender에게)
            alarmService.sendAlarm(
                    match.getSender().getId(),
                    match.getReceiver().getNickname(),
                    Alarm.AlarmType.MATCH_REQUEST,
                    match.getReceiver().getNickname() + " 님이 매칭을 수락했습니다. 채팅을 시작해보세요!"
            );

            // 💡 여기서 채팅방 생성도 같이 하면 좋음
        }
    }


    @Override
    public void cancelRequest(Long matchId, Long senderId) {
        Matching match = matchingRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("매칭 없음"));

        if (!match.getSender().getId().equals(senderId)) {
            throw new IllegalStateException("본인이 보낸 요청만 취소할 수 있습니다.");
        }

        if (match.getStatus() != MatchingStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 매칭은 취소할 수 없습니다.");
        }

        matchingRepository.delete(match);
    }
}
