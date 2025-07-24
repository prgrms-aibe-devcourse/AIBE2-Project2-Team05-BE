package com.main.TravelMate.admin.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ManageMatchingRequest {
    private Long matchId;
    private String status; // "APPROVED", "REJECTED_BY_ADMIN", "UNDER_REVIEW"
    private String notes;  // 처리 사유
}
