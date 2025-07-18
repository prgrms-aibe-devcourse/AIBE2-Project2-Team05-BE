package com.main.TravelMate.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageRequestDTO {
    private Long chatRoomId;
    private Long senderId;
    private String message;
}
