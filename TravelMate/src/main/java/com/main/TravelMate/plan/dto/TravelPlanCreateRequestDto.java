package com.main.TravelMate.plan.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TravelPlanCreateRequestDto {
    private String title;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private String interests;
    private Integer budget;
    private Integer numberOfPeople;
}
