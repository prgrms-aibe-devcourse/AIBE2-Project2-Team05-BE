package com.main.TravelMate.chat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ChatMessageResponseDTO {
    private Long id;
    private Long senderId;
    private String message;
    private LocalDateTime sentAt;
}
