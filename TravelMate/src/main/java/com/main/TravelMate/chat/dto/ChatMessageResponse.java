package com.main.TravelMate.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ChatMessageResponse {
    private String senderEmail;
    private String message;
    private LocalDateTime sentAt;
}
