package com.main.TravelMate.admin.entity;

import com.main.TravelMate.admin.domain.MatchingManageStatus;
import com.main.TravelMate.match.entity.Matching;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "managed_matching_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManagedMatchingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_id", unique = true)
    private Matching matching;

    @ManyToOne(fetch = FetchType.LAZY)
    private Admin admin;

    @Enumerated(EnumType.STRING)
    private MatchingManageStatus status; // APPROVED, REJECTED_BY_ADMIN, UNDER_REVIEW

    private String notes;

    private LocalDateTime updatedAt;
}