package com.main.TravelMate.chat.dto;

import lombok.Data;

@Data
public class ChatMessageRequest {
    private Long chatRoomId;
    private String message;
}