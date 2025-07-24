package com.main.TravelMate.plan.repository;

import com.main.TravelMate.match.entity.TravelStyle;
import com.main.TravelMate.plan.entity.TravelPlan;
import com.main.TravelMate.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TravelPlanRepository extends JpaRepository<TravelPlan, Long> {
    List<TravelPlan> findByUserId(Long userId);
    List<TravelPlan> findByUser(User user);
    @Query("SELECT p FROM TravelPlan p WHERE p.user.id != :userId")
    List<TravelPlan> findRecruitingPlansExcludingUser(@Param("userId") Long userId);
    Optional<TravelPlan> findFirstByUserIdOrderByStartDateDesc(Long userId);
}