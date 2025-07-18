package com.main.TravelMate.chat.repository;

import com.main.TravelMate.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByMatchingRequestId(Long matchingId);
}
