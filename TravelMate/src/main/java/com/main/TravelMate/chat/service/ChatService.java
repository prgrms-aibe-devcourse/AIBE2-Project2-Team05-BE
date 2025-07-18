package com.main.TravelMate.chat.service;

import com.main.TravelMate.chat.dto.ChatMessageResponse;
import com.main.TravelMate.chat.entity.ChatMessage;
import com.main.TravelMate.chat.entity.ChatRoom;
import com.main.TravelMate.chat.repository.ChatMessageRepository;
import com.main.TravelMate.chat.repository.ChatRoomRepository;
import com.main.TravelMate.matching.entity.MatchingRequest;
import com.main.TravelMate.matching.repository.MatchingRequestRepository;
import com.main.TravelMate.user.entity.User;
import com.main.TravelMate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MatchingRequestRepository matchingRequestRepository;
    private final UserRepository userRepository;

    public ChatRoom createChatRoom(Long matchingId) {
        MatchingRequest matching = matchingRequestRepository.findById(matchingId)
                .orElseThrow(() -> new IllegalArgumentException("매칭 요청 없음"));

        // 중복 생성 방지
        return chatRoomRepository.findByMatchingRequestId(matchingId)
                .orElseGet(() -> chatRoomRepository.save(ChatRoom.builder()
                        .matchingRequest(matching)
                        .build()));
    }

    public void sendMessage(Long chatRoomId, String senderEmail, String content) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방 없음"));

        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new UsernameNotFoundException("사용자 없음"));

        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .message(content)
                .build();

        chatMessageRepository.save(message);
    }

    public List<ChatMessageResponse> getMessages(Long roomId) {
        return chatMessageRepository.findByChatRoomIdOrderBySentAtAsc(roomId)
                .stream()
                .map(m -> new ChatMessageResponse(
                        m.getSender().getEmail(),
                        m.getMessage(),
                        m.getSentAt()
                ))
                .collect(Collectors.toList());
    }
}
