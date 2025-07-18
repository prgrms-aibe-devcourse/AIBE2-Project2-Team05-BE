package com.main.TravelMate.plan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TravelPlanResponseDto {
    private Long id;
    private String title;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private String interests;
    private Integer budget;
    private Integer numberOfPeople;
    private LocalDateTime createdAt;
}