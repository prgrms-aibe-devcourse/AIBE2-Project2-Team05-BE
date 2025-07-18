package com.main.TravelMate.plan.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TravelPlanCreateRequestDto {
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private String interests;
    private String title;
    private Integer budget;
    private String destination;
    private Integer numberOfPeople;
}
