package com.example.chat.dto;

import com.example.chat.WebSocketTextMessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WebSocketTextMessage {
    /**
     * 메시지의 타입
     */
    private WebSocketTextMessageType type;   // INVITE, NEW_MESSAGE

    /**
     * 어떤 방을 target으로 전송하는지 데이터
     */
    private Long roomId;

    /**
     * 방 이름 (채팅방 초대시에만 사용)
     */
    private String roomName;

    /**
     * 메시지 내용
     */
    private String message;

    /**
     * 메시지를 보낸 유저 이름
     */
    private String senderName;

    /**
     * 메시지를 보낸 유저 ID
     */
    private Long userId;

    /**
     * 메시지를 받을 대상 유저 ID 배열 -> 초대 알림 전송 시에만 데이터가 포함됨
     */
    private List<Long> targetUserIds;
}
