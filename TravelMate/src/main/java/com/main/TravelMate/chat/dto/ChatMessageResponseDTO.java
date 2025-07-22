package com.main.TravelMate.chat.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponseDTO {
    private Long id;
    private Long senderId;
    private String message;
    private LocalDateTime sentAt;
}
