package com.main.TravelMate.chat.service;

import com.main.TravelMate.chat.domain.ChatMessage;
import com.main.TravelMate.chat.domain.ChatRoom;
import com.main.TravelMate.chat.domain.MatchingRequest;
import com.main.TravelMate.chat.dto.ChatMessageRequestDTO;
import com.main.TravelMate.chat.dto.ChatMessageResponseDTO;
import com.main.TravelMate.chat.repository.ChatMessageRepository;
import com.main.TravelMate.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    public ChatRoom createChatRoom(Long matchingId) {
        ChatRoom room = new ChatRoom();
        MatchingRequest matching = new MatchingRequest();
        matching.setId(matchingId);
        room.setMatchingRequest(matching);
        return chatRoomRepository.save(room);
    }

    public ChatMessageResponseDTO sendMessage(ChatMessageRequestDTO dto) {
        ChatRoom room = chatRoomRepository.findById(dto.getChatRoomId())
                .orElseThrow(() -> new RuntimeException("채팅방 없음"));

        ChatMessage message = ChatMessage.builder()
                .chatRoom(room)
                .senderId(dto.getSenderId())
                .message(dto.getMessage())
                .sentAt(LocalDateTime.now())
                .build();

        chatMessageRepository.save(message);

        return ChatMessageResponseDTO.builder()
                .id(message.getId())
                .senderId(message.getSenderId())
                .message(message.getMessage())
                .sentAt(message.getSentAt())
                .build();
    }

    public List<ChatMessageResponseDTO> getChatMessages(Long chatRoomId) {
        return chatMessageRepository.findByChatRoomIdOrderBySentAtAsc(chatRoomId)
                .stream()
                .map(msg -> ChatMessageResponseDTO.builder()
                        .id(msg.getId())
                        .senderId(msg.getSenderId())
                        .message(msg.getMessage())
                        .sentAt(msg.getSentAt())
                        .build())
                .collect(Collectors.toList());
    }
}

