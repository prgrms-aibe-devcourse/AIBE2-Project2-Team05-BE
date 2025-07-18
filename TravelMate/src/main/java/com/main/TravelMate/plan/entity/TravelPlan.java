package com.main.TravelMate.plan.entity;

import com.main.TravelMate.user.entity.User;
import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(nullable = false)
    private String title;

    private String location;

    private LocalDate startDate;
    private LocalDate endDate;

    @Lob
    private String description;

    private String interests;


    private Integer budget;

    private String destination;

    private Integer numberOfPeople;

    private LocalDateTime createdAt = LocalDateTime.now();
}