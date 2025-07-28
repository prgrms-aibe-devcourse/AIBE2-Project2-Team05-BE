package com.main.TravelMate.match.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.TravelMate.alarm.domain.Alarm;
import com.main.TravelMate.alarm.service.AlarmService;
import com.main.TravelMate.feed.domain.TravelStatus;
import com.main.TravelMate.feed.entity.TravelFeed;
import com.main.TravelMate.feed.repository.TravelFeedRepository;
import com.main.TravelMate.match.domain.MatchingStatus;
import com.main.TravelMate.match.dto.MatchFilterRequestDto;
import com.main.TravelMate.match.dto.MatchRecommendationDto;
import com.main.TravelMate.match.dto.MatchRequestDto;
import com.main.TravelMate.match.dto.MatchResponseDto;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchingServiceImpl implements MatchingService {

    private final UserRepository userRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final MatchingRepository matchingRepository;
    private final AlarmService alarmService;
    private final TravelFeedRepository travelFeedRepository;

    @Override
    public List<MatchRecommendationDto> getRecommendations(Long userId) {
        User me = userRepository.findById(userId).orElseThrow();
        TravelPlan myPlan = travelPlanRepository.findFirstByUserIdOrderByStartDateDesc(userId)
                .orElseThrow(() -> new RuntimeException("플랜 없음"));

        List<Long> excludedPlanIds = matchingRepository
                .findBySenderIdAndStatusIn(userId, List.of(
                        MatchingStatus.PENDING,
                        MatchingStatus.ACCEPTED,
                        MatchingStatus.REJECTED
                ))
                .stream()
                .map(m -> m.getPlan().getId())
                .toList();


        List<TravelPlan> candidates = travelPlanRepository
                .findRecruitingPlansExcludingUser(userId)
                .stream()
                .filter(p -> !excludedPlanIds.contains(p.getId()))
                .filter(p -> p.getCurrentPeople() + myPlan.getCurrentPeople() <= p.getNumberOfPeople())
                .filter(p -> {
                    // 🔍 travel_feed.travel_status가 RECRUITING(모집중)인지 확인
                    Optional<TravelFeed> feedOpt = travelFeedRepository.findByTravelPlan_Id(p.getId());
                    return feedOpt.isPresent() && feedOpt.get().getTravelStatus() == TravelStatus.RECRUITING;
                })
                .toList();

        return candidates.stream()
                .map(p -> {
                    int score = calculateCompatibilityScore(myPlan, p);
                    if (score >= 60) {
                        return new MatchRecommendationDto(
                                p.getUser().getId(),
                                p.getUser().getNickname(),
                                p.getLocation(),
                                p.getStartDate(),
                                p.getEndDate(),
                                p.getId(),
                                score
                        );
                    } else {
                        return null; // 점수 낮으면 추천 제외
                    }
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(MatchRecommendationDto::getCompatibilityScore).reversed()) // 높은 점수 우선
                .toList();
    }

    private int calculateCompatibilityScore(TravelPlan myPlan, TravelPlan target) {
        int score = 0;

        System.out.println("============== 유사도 계산 시작 ==============");
        System.out.println("👉 대상 플랜 ID: " + target.getId() + ", 유저: " + target.getUser().getNickname());

        // 목적지
        if (isSameDestination(myPlan.getLocation(), target.getLocation())) {
            score += 35;
            System.out.println("✅ 목적지 비슷 +35");
        }

        // 일정 겹침
        int overlap = calculateOverlappingDays(myPlan.getStartDate(), myPlan.getEndDate(),
                target.getStartDate(), target.getEndDate());
        if (overlap > 0) {
            long myDays = ChronoUnit.DAYS.between(myPlan.getStartDate(), myPlan.getEndDate()) + 1;
            long otherDays = ChronoUnit.DAYS.between(target.getStartDate(), target.getEndDate()) + 1;

            double myRatio = (double) overlap / myDays;
            double otherRatio = (double) overlap / otherDays;
            double avgRatio = (myRatio + otherRatio) / 2;

            int overlapScore = (int) (avgRatio * 35);
            score += overlapScore;
            System.out.println("✅ 일정 겹침 + " + overlapScore);
        }

        // 여행일수 차이
        long myDays = ChronoUnit.DAYS.between(myPlan.getStartDate(), myPlan.getEndDate()) + 1;
        long otherDays = ChronoUnit.DAYS.between(target.getStartDate(), target.getEndDate()) + 1;
        long diffDays = Math.abs(myDays - otherDays);
        if (diffDays <= 2) {
            score += 15;
            System.out.println("✅ 일수차이 ±2일 이내 +15");
        } else if (diffDays <= 4) {
            score += 8;
            System.out.println("✅ 일수차이 ±4일 이내 +8");
        }

        // 인원수
        int combinedPeople = myPlan.getCurrentPeople() + target.getCurrentPeople();
        if (combinedPeople <= target.getNumberOfPeople()) {
            score += 15;
            System.out.println("✅ 인원수 조건 만족 (정원 이하) +15");
        }

        // 스타일
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<String> myStyles = mapper.readValue(myPlan.getStyles(), new TypeReference<>() {});
            List<String> targetStyles = mapper.readValue(target.getStyles(), new TypeReference<>() {});
            long common = myStyles.stream().filter(targetStyles::contains).count();
            if (common > 0) {
                int styleScore = (int) Math.min(common * 5, 10);
                score += styleScore;
                System.out.println("✅ 스타일 공통 " + common + "개 + " + styleScore);
            }
        } catch (Exception e) {
            System.out.println("⚠️ 스타일 비교 실패: " + e.getMessage());
        }

        System.out.println("➡️ 총 유사도 점수: " + score);
        System.out.println("==============================================");

        return Math.min(score, 100);
    }


    private int calculateOverlappingDays(LocalDate aStart, LocalDate aEnd, LocalDate bStart, LocalDate bEnd) {
        LocalDate overlapStart = aStart.isAfter(bStart) ? aStart : bStart;
        LocalDate overlapEnd = aEnd.isBefore(bEnd) ? aEnd : bEnd;
        if (overlapStart.isAfter(overlapEnd)) return 0;
        return (int) ChronoUnit.DAYS.between(overlapStart, overlapEnd) + 1;
    }


    private boolean isSameDestination(String myLocation, String targetLocation) {
        Map<String, String> destinationMap = Map.ofEntries(
                // 서울
                Map.entry("서울", "서울"),
                Map.entry("강남", "서울"),
                Map.entry("홍대", "서울"),
                Map.entry("이태원", "서울"),
                Map.entry("종로", "서울"),
                Map.entry("잠실", "서울"),
                Map.entry("명동", "서울"),
                Map.entry("한강", "서울"),
                Map.entry("건대", "서울"),
                Map.entry("성수", "서울"),
                Map.entry("북촌", "서울"),
                Map.entry("남산", "서울"),
                Map.entry("광화문", "서울"),
                Map.entry("여의도", "서울"),

                // 부산
                Map.entry("부산", "부산"),
                Map.entry("해운대", "부산"),
                Map.entry("광안리", "부산"),
                Map.entry("남포동", "부산"),
                Map.entry("서면", "부산"),
                Map.entry("태종대", "부산"),
                Map.entry("송정", "부산"),
                Map.entry("감천문화마을", "부산"),
                Map.entry("광복로", "부산"),
                Map.entry("부산역", "부산"),

                // 대구
                Map.entry("대구", "대구"),
                Map.entry("동성로", "대구"),
                Map.entry("앞산", "대구"),
                Map.entry("서문시장", "대구"),
                Map.entry("수성못", "대구"),
                Map.entry("이월드", "대구"),
                Map.entry("팔공산", "대구"),

                // 제주도
                Map.entry("제주도", "제주도"),
                Map.entry("제주시", "제주도"),
                Map.entry("서귀포", "제주도"),
                Map.entry("성산", "제주도"),
                Map.entry("함덕", "제주도"),
                Map.entry("협재", "제주도"),
                Map.entry("우도", "제주도"),
                Map.entry("한라산", "제주도"),

                // 인천
                Map.entry("인천", "인천"),
                Map.entry("송도", "인천"),
                Map.entry("을왕리", "인천"),
                Map.entry("월미도", "인천"),
                Map.entry("차이나타운", "인천"),
                Map.entry("영종도", "인천"),

                // 강원도
                Map.entry("강원도", "강원도"),
                Map.entry("강릉", "강원도"),
                Map.entry("속초", "강원도"),
                Map.entry("양양", "강원도"),
                Map.entry("춘천", "강원도"),
                Map.entry("평창", "강원도"),
                Map.entry("홍천", "강원도"),
                Map.entry("정선", "강원도"),

                // 경기도
                Map.entry("경기도", "경기도"),
                Map.entry("수원", "경기도"),
                Map.entry("가평", "경기도"),
                Map.entry("양평", "경기도"),
                Map.entry("용인", "경기도"),
                Map.entry("파주", "경기도"),
                Map.entry("남양주", "경기도"),
                Map.entry("일산", "경기도"),
                Map.entry("안산", "경기도"),

                // 전라도
                Map.entry("전라도", "전라도"),
                Map.entry("전주", "전라도"),
                Map.entry("여수", "전라도"),
                Map.entry("순천", "전라도"),
                Map.entry("광주", "전라도"),
                Map.entry("남원", "전라도"),
                Map.entry("군산", "전라도"),

                // 경상도
                Map.entry("경상도", "경상도"),
                Map.entry("경주", "경상도"),
                Map.entry("포항", "경상도"),
                Map.entry("통영", "경상도"),
                Map.entry("창원", "경상도"),
                Map.entry("울산", "경상도"),
                Map.entry("하동", "경상도"),
                Map.entry("남해", "경상도")
        );

        String normalizedMy = destinationMap.getOrDefault(myLocation, myLocation);
        String normalizedTarget = destinationMap.getOrDefault(targetLocation, targetLocation);
        return normalizedMy.equals(normalizedTarget);
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

        // 🔔 알림 추가: 받는 사람에게 알림
        alarmService.sendAlarm(
                match.getReceiver().getId(),
                match.getSender().getNickname(),
                Alarm.AlarmType.MATCH_REQUEST,
                match.getSender().getNickname() + " 님이 보낸 매칭 요청이 취소되었습니다."
        );

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

    @Override
    public void cancelAcceptedMatch(Long matchId, Long userId) {
        Matching match = matchingRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("매칭 없음"));

        if (match.getStatus() != MatchingStatus.ACCEPTED) {
            throw new IllegalStateException("수락된 매칭만 취소할 수 있습니다.");
        }

        if (!match.getSender().getId().equals(userId) && !match.getReceiver().getId().equals(userId)) {
            throw new IllegalStateException("본인만 취소할 수 있습니다.");
        }

        TravelPlan senderPlan = travelPlanRepository.findFirstByUserIdOrderByStartDateDesc(match.getSender().getId())
                .orElseThrow();
        TravelPlan receiverPlan = travelPlanRepository.findFirstByUserIdOrderByStartDateDesc(match.getReceiver().getId())
                .orElseThrow();

        receiverPlan.setCurrentPeople(receiverPlan.getCurrentPeople() - senderPlan.getCurrentPeople());
        senderPlan.setRecruiting(true);
        if (receiverPlan.getCurrentPeople() < receiverPlan.getNumberOfPeople()) {
            receiverPlan.setRecruiting(true);
        }

        // 🔔 알림 추가
        Long opponentId = match.getSender().getId().equals(userId)
                ? match.getReceiver().getId()
                : match.getSender().getId();
        User cancelUser = userRepository.findById(userId).orElseThrow();
        alarmService.sendAlarm(
                opponentId,
                cancelUser.getNickname(),
                Alarm.AlarmType.MATCH_REQUEST,
                cancelUser.getNickname() + " 님이 매칭 수락을 취소했습니다."
        );

        matchingRepository.delete(match);
        travelPlanRepository.save(senderPlan);
        travelPlanRepository.save(receiverPlan);
    }


    private MatchResponseDto toDto(Matching match) {
        return new MatchResponseDto(
                match.getId(),
                match.getStatus()
        );
    }



    @Override
    public void updateTravelStatus(Long userId, Long travelPlanId, String status) {
        TravelPlan plan = travelPlanRepository.findById(travelPlanId)
                .orElseThrow(() -> new RuntimeException("해당 여행 계획을 찾을 수 없습니다."));

        if (!plan.getUser().getId().equals(userId)) {
            throw new IllegalStateException("여행 작성자만 여행 상태를 변경할 수 있습니다.");
        }

        TravelFeed feed = travelFeedRepository.findByTravelPlan_Id(travelPlanId)
                .orElseThrow(() -> new RuntimeException("해당 여행 계획에 연결된 피드를 찾을 수 없습니다."));

        try {
            // 입력된 문자열을 대문자로 변환 후 enum으로
            TravelStatus travelStatus = TravelStatus.valueOf(status.toUpperCase());

            feed.setTravelStatus(travelStatus);
            travelFeedRepository.save(feed);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 상태입니다. (RECRUITING, TRAVELING, COMPLETED 중 하나여야 합니다)");
        }
    }


    @Override
    public List<MatchResponseDto> getMySentRequests(Long userId) {
        return matchingRepository.findAllBySenderId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchResponseDto> getMyReceivedRequests(Long userId) {
        return matchingRepository.findByReceiverIdAndStatus(userId, MatchingStatus.PENDING).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchResponseDto> getMyAcceptedMatches(Long userId) {
        return matchingRepository.findAll().stream()
                .filter(m -> m.getStatus() == MatchingStatus.ACCEPTED &&
                        (m.getSender().getId().equals(userId) || m.getReceiver().getId().equals(userId)))
                .map(this::toDto)
                .collect(Collectors.toList());
    }


    @Override
    public List<MatchRecommendationDto> filterRecommendations(MatchFilterRequestDto filter) {
        List<TravelPlan> all = travelPlanRepository.findRecruitingPlans(); // 모집중인 플랜만

        ObjectMapper mapper = new ObjectMapper();

        return all.stream()
                .filter(p -> {
                    // 🔸 내 플랜 제외
                    if (filter.getUserId().equals(p.getUser().getId())) {
                        return false;
                    }

                    // 🔸 내가 거절한 플랜 제외
                    boolean rejected = matchingRepository.existsBySenderIdAndReceiverIdAndPlanIdAndStatus(
                            filter.getUserId(), p.getUser().getId(), p.getId(), MatchingStatus.REJECTED
                    );
                    if (rejected) return false;

                    // 🔸 지역 필터
                    if (filter.getLocation() != null && !p.getLocation().contains(filter.getLocation())) {
                        return false;
                    }

                    // 🔸 날짜 필터
                    if (filter.getStartDate() != null && filter.getEndDate() != null) {
                        if (p.getStartDate().isAfter(filter.getEndDate()) || p.getEndDate().isBefore(filter.getStartDate())) {
                            return false;
                        }
                    }

                    // 🔸 스타일 필터
                    if (filter.getStyles() != null && !filter.getStyles().isEmpty()) {
                        try {
                            List<String> planStyles = mapper.readValue(p.getStyles(), new TypeReference<>() {});
                            boolean hasCommon = planStyles.stream().anyMatch(filter.getStyles()::contains);
                            if (!hasCommon) return false;
                        } catch (Exception e) {
                            return false;
                        }
                    }

                    return true;
                })

                .map(p -> new MatchRecommendationDto(
                        p.getUser().getId(),
                        p.getUser().getNickname(),
                        p.getLocation(),
                        p.getStartDate(),
                        p.getEndDate(),
                        p.getId(),
                        0 // 유사도 없음
                ))
                .toList();
    }



}
