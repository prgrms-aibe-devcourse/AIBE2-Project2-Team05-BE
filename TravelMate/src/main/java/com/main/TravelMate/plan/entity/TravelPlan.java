package com.main.TravelMate.plan.entity;

import com.main.TravelMate.user.entity.User;
import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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


    private long budget;

    private String destination;

    private Integer numberOfPeople;

    private int currentPeople;


    @OneToMany(mappedBy = "travelPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TravelDay> days;


    @Column(length = 50)
    private String preferredGender;

    @Column(length = 50)
    private String preferredAgeRange;

    @Column(length = 50)
    private String preferredLanguage;

    @Column(columnDefinition = "TEXT")
    private String matchingNote;

    @Column(columnDefinition = "TEXT")
    private String accommodationInfo;

    @Column(columnDefinition = "TEXT")
    private String transportationInfo;

    @Column(columnDefinition = "TEXT")
    private String extraMemo;

    @Builder.Default
    @Column(nullable = false)
    private boolean recruiting = true;

    @Column(columnDefinition = "TEXT")  // 또는 VARCHAR(1000) 등으로 맞춰줘도 됨
    private String styles;



    private LocalDateTime createdAt = LocalDateTime.now();
}