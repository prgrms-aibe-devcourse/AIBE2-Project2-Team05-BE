package com.main.TravelMate.plan.service;

import com.main.TravelMate.plan.dto.TravelPlanCreateRequestDto;
import com.main.TravelMate.plan.dto.TravelPlanResponseDto;
import com.main.TravelMate.plan.entity.TravelPlan;
import com.main.TravelMate.plan.repository.TravelPlanRepository;
import com.main.TravelMate.user.entity.User;
import com.main.TravelMate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TravelPlanService {

    private final TravelPlanRepository planRepository;
    private final UserRepository userRepository;

    public void createPlan(String email, TravelPlanCreateRequestDto request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("유저 없음"));

        TravelPlan plan = TravelPlan.builder()
                .user(user)
                .title(request.getTitle())
                .destination(request.getDestination())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .description(request.getDescription())
                .interests(request.getInterests())
                .numberOfPeople(request.getNumberOfPeople())
                .budget(request.getBudget())
                .createdAt(LocalDateTime.now())
                .build();

        planRepository.save(plan);
    }

    public List<TravelPlanResponseDto> getUserPlans(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("유저 없음"));

        return planRepository.findByUserId(user.getId())
                .stream()
                .map(plan -> new TravelPlanResponseDto(
                        plan.getId(),
                        plan.getTitle(),
                        plan.getDestination(),
                        plan.getStartDate(),
                        plan.getEndDate(),
                        plan.getDescription(),
                        plan.getInterests(),
                        plan.getBudget(),
                        plan.getNumberOfPeople(),
                        plan.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
}
