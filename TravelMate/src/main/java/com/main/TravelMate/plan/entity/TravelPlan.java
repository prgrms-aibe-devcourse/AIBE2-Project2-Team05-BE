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

    private String location;

    private LocalDate startDate;
    private LocalDate endDate;

    @Lob
    private String description;

    private String interests;

    private LocalDateTime createdAt = LocalDateTime.now();
}