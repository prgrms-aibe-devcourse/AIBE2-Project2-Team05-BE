package com.main.TravelMate.match.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class MatchFilterRequestDto {
    private String location; // optional
    private LocalDate startDate; // optional
    private LocalDate endDate;   // optional
    private List<String> styles; // optional
    private Long userId;
}