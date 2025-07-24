//package com.main.TravelMate.chat.entity;
//
//
//import com.main.TravelMate.match.entity.MatchingRequest;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Getter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class ChatRoom {
//    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @OneToOne
//    @JoinColumn(name = "matching_id")
//    private MatchingRequest matching;
//
//    private LocalDateTime createdAt;
//}