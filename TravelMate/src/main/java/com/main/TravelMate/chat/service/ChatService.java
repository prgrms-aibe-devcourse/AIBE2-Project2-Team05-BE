package com.main.TravelMate.chat.service;

import com.main.TravelMate.chat.domain.ChatMessage;
import com.main.TravelMate.chat.domain.ChatRoom;
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

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;

    public ChatRoom createRoom(Long matchingId) {
        ChatRoom room = ChatRoom.builder()
                .matchingId(matchingId)
                .createdAt(LocalDateTime.now())
                .build();
        return chatRoomRepository.save(room);
    }

    public ChatRoom getRoom(Long id) {
        return chatRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("채팅방 없음"));
    }

    public ChatMessageResponseDTO saveMessage(ChatMessageRequestDTO dto) {
        ChatMessage message = ChatMessage.builder()
                .chatRoomId(dto.getChatRoomId())
                .senderId(dto.getSenderId())
                .message(dto.getMessage())
                .sentAt(LocalDateTime.now())
                .build();

        ChatMessage saved = chatMessageRepository.save(message);

        return ChatMessageResponseDTO.builder()
                .id(saved.getId())
                .senderId(saved.getSenderId())
                .message(saved.getMessage())
                .sentAt(saved.getSentAt())
                .build();
    }

    public List<ChatMessageResponseDTO> getNewMessages(Long chatRoomId, Long lastMessageId) {
        return chatMessageRepository.findByChatRoomIdAndIdGreaterThanOrderByIdAsc(chatRoomId, lastMessageId)
                .stream()
                .map(m -> ChatMessageResponseDTO.builder()
                        .id(m.getId())
                        .senderId(m.getSenderId())
                        .message(m.getMessage())
                        .sentAt(m.getSentAt())
                        .build())
                .collect(Collectors.toList());
    }
}
