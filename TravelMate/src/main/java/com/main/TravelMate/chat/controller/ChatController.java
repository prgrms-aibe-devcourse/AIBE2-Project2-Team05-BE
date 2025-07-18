package com.main.TravelMate.chat.controller;

import com.main.TravelMate.chat.dto.ChatMessageRequest;
import com.main.TravelMate.chat.dto.ChatMessageResponse;
import com.main.TravelMate.chat.entity.ChatRoom;
import com.main.TravelMate.chat.service.ChatService;
import com.main.TravelMate.common.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/room")
    public ResponseEntity<Long> createRoom(@RequestParam Long matchingId) {
        ChatRoom room = chatService.createChatRoom(matchingId);
        return ResponseEntity.ok(room.getId());
    }

    @PostMapping("/message")
    public ResponseEntity<String> sendMessage(@RequestBody ChatMessageRequest request,
                                              Authentication authentication) {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        chatService.sendMessage(request.getChatRoomId(), user.getUsername(), request.getMessage());
        return ResponseEntity.ok("메시지 전송 완료");
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(@PathVariable Long roomId) {
        return ResponseEntity.ok(chatService.getMessages(roomId));
    }
}
