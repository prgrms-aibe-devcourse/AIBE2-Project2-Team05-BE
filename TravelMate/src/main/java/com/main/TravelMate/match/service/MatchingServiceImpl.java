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
        TravelPlan myPlan = travelPlanRepository.findFirstByUserIdOrderByStartDateDesc(userId)
                .orElseThrow(() -> new RuntimeException("플랜 없음"));

        // ❗ 내가 이미 거절하거나 요청한 플랜 제외
        List<Long> excludedPlanIds = matchingRepository.findAllBySenderId(userId).stream()
                .map(m -> m.getPlan().getId())
                .toList();

        List<TravelPlan> candidates = travelPlanRepository
                .findRecruitingPlansExcludingUser(userId)
                .stream()
                .filter(p -> !excludedPlanIds.contains(p.getId()))
                .toList();

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
            // 알림 전송
            alarmService.sendAlarm(
                    match.getSender().getId(),
                    match.getReceiver().getNickname(),
                    Alarm.AlarmType.MATCH_REQUEST,
                    match.getReceiver().getNickname() + " 님이 매칭을 수락했습니다. 채팅을 시작해보세요!"
            );

            User sender = match.getSender();
            User receiver = match.getReceiver();

            TravelPlan senderPlan = travelPlanRepository.findFirstByUserIdOrderByStartDateDesc(sender.getId())
                    .orElse(null);
            TravelPlan receiverPlan = travelPlanRepository.findFirstByUserIdOrderByStartDateDesc(receiver.getId())
                    .orElse(null);

            if (senderPlan != null && receiverPlan != null) {

                // 1. senderPlan 모집 종료 (매칭 요청했으므로 더 이상 안 받음)
                senderPlan.setRecruiting(false);

                // 2. receiverPlan의 현재 인원 += senderPlan의 현재 인원
                int updatedReceiverPeople = receiverPlan.getCurrentPeople() + senderPlan.getCurrentPeople();
                receiverPlan.setCurrentPeople(updatedReceiverPeople);

                // 3. receiverPlan도 마감되었는지 확인
                if (updatedReceiverPeople >= receiverPlan.getNumberOfPeople()) {
                    receiverPlan.setRecruiting(false);
                }

                travelPlanRepository.save(senderPlan);
                travelPlanRepository.save(receiverPlan);
            }

            // 💬 채팅방 생성 등 추가 로직 가능
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



    @Override
    public void rejectPlan(Long senderId, Long planId) {
        TravelPlan plan = travelPlanRepository.findById(planId).orElseThrow();
        User sender = userRepository.findById(senderId).orElseThrow();
        User receiver = plan.getUser();

        // 이미 거절한 이력이 있으면 중복 저장 안 하게 처리
        boolean alreadyExists = matchingRepository
                .existsBySenderIdAndReceiverIdAndPlanId(senderId, receiver.getId(), planId);
        if (alreadyExists) return;

        Matching reject = Matching.builder()
                .sender(sender)
                .receiver(receiver)
                .plan(plan)
                .status(MatchingStatus.REJECTED)
                .createdAt(LocalDateTime.now())
                .build();

        matchingRepository.save(reject);
    }
}
