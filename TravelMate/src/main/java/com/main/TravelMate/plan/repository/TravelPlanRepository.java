package com.main.TravelMate.plan.repository;

import com.main.TravelMate.match.entity.TravelStyle;
import com.main.TravelMate.plan.entity.TravelPlan;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TravelPlanRepository extends JpaRepository<TravelPlan, Long> {
    List<TravelPlan> findByUserId(Long userId);

    Page<TravelPlan> findCompatibleTravelPlansByStyle(@NotBlank(message = "목적지는 필수입니다") String destination, @NotNull(message = "여행 시작일은 필수입니다") LocalDate startDate, @NotNull(message = "여행 종료일은 필수입니다") LocalDate endDate, Long currentUserId, TravelStyle travelStyle, Pageable pageable);
}