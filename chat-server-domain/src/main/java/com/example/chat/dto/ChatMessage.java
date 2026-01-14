package com.example.chat.dto;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 채팅 메시지 모델 -> MongoDB 저장형태
 */
@Document(collection = "chat_messages")
@CompoundIndex(
        name = "room_sent_idx",
        def = "{ 'roomId': 1, 'sentAt': -1 }"
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    private String id;   // MongoDB _id

    /**
     * 채팅방 ID
     */
    private Long roomId;

    /**
     * 채팅을 보낸 유저 ID
     */
    private Long userId;

    /**
     * 채팅을 보낸 유저 이름
     */
    private String senderName;

    /**
     * 채팅 메시지 내용
     */
    private String message;

    /**
     * 채팅 메시지 전송 시각 (밀리초)
     */
    private Long sentAt;
}
