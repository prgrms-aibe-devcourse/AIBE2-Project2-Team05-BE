package com.main.TravelMate.matching.entity;

import com.main.TravelMate.matching.domain.MatchingStatus;
import com.main.TravelMate.plan.entity.TravelPlan;
import com.main.TravelMate.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User sender;

    @ManyToOne
    private User receiver;

    @ManyToOne
    private TravelPlan plan;

    @Enumerated(EnumType.STRING)
    private MatchingStatus status = MatchingStatus.PENDING; // 기본값

    private LocalDateTime createdAt = LocalDateTime.now();
}
