package com.main.TravelMate.chat.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponseDTO {
    private Long id;  // 메시지의 고유 ID
    private Long chatRoomId;
    private Long senderId;
    private String message;
    private LocalDateTime sentAt;
}
