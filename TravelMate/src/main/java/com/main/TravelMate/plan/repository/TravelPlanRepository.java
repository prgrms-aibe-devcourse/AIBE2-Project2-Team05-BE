package com.main.TravelMate.plan.repository;

import com.main.TravelMate.match.entity.TravelStyle;
import com.main.TravelMate.plan.entity.TravelPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TravelPlanRepository extends JpaRepository<TravelPlan, Long> {
    
    // 사용자별 여행 계획 조회
    List<TravelPlan> findByUserId(Long userId);
    
    // 목적지별 여행 계획 검색 (페이징 지원)
    Page<TravelPlan> findByDestinationContainingIgnoreCase(String destination, Pageable pageable);
    
    // 목적지와 날짜 범위로 호환 가능한 여행 계획 검색
    @Query("SELECT tp FROM TravelPlan tp WHERE " +
           "LOWER(tp.destination) LIKE LOWER(CONCAT('%', :destination, '%')) AND " +
           "tp.user.id != :excludeUserId AND " +
           "((tp.startDate <= :endDate AND tp.endDate >= :startDate))")
    Page<TravelPlan> findCompatibleTravelPlans(
        @Param("destination") String destination,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("excludeUserId") Long excludeUserId,
        Pageable pageable
    );
    
    // 목적지, 날짜, 여행 스타일로 호환 가능한 여행 계획 검색
    @Query("SELECT tp FROM TravelPlan tp WHERE " +
           "LOWER(tp.destination) LIKE LOWER(CONCAT('%', :destination, '%')) AND " +
           "tp.user.id != :excludeUserId AND " +
           "((tp.startDate <= :endDate AND tp.endDate >= :startDate)) AND " +
           "(:travelStyle IS NULL OR tp.travelStyle = :travelStyle)")
    Page<TravelPlan> findCompatibleTravelPlansByStyle(
        @Param("destination") String destination,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("excludeUserId") Long excludeUserId,
        @Param("travelStyle") TravelStyle travelStyle,
        Pageable pageable
    );
    
    // 여행 스타일별 여행 계획 검색
    Page<TravelPlan> findByTravelStyle(TravelStyle travelStyle, Pageable pageable);
    
    // 특정 날짜 범위 내의 여행 계획 검색
    @Query("SELECT tp FROM TravelPlan tp WHERE " +
           "tp.startDate <= :endDate AND tp.endDate >= :startDate")
    Page<TravelPlan> findByDateRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        Pageable pageable
    );
    
    // 사용자가 이미 매칭 요청을 보낸 여행 계획들을 제외한 호환 가능한 계획 검색
    @Query("SELECT tp FROM TravelPlan tp WHERE " +
           "LOWER(tp.destination) LIKE LOWER(CONCAT('%', :destination, '%')) AND " +
           "tp.user.id != :excludeUserId AND " +
           "((tp.startDate <= :endDate AND tp.endDate >= :startDate)) AND " +
           "tp.id NOT IN (" +
           "    SELECT m.travelPlan.id FROM Match m WHERE " +
           "    m.requester.id = :excludeUserId AND m.travelPlan.id IS NOT NULL" +
           ")")
    Page<TravelPlan> findAvailableCompatibleTravelPlans(
        @Param("destination") String destination,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("excludeUserId") Long excludeUserId,
        Pageable pageable
    );
    
    // 인기 목적지 조회 (여행 계획 수 기준)
    @Query("SELECT tp.destination, COUNT(tp) as planCount FROM TravelPlan tp " +
           "WHERE tp.destination IS NOT NULL " +
           "GROUP BY tp.destination " +
           "ORDER BY planCount DESC")
    List<Object[]> findPopularDestinations(Pageable pageable);
    
    // 활성 여행 계획 조회 (현재 날짜 이후)
    @Query("SELECT tp FROM TravelPlan tp WHERE tp.endDate >= CURRENT_DATE")
    Page<TravelPlan> findActiveTravelPlans(Pageable pageable);
}