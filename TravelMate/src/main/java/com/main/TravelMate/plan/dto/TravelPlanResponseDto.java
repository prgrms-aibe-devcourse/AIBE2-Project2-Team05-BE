package com.main.TravelMate.plan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TravelPlanResponseDto {
    private Long id;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private String interests;
    private LocalDateTime createdAt;
}