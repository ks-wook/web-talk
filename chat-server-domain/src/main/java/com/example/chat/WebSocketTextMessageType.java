package com.example.chat;

/**
 * Redis pub/sub 메시지 타입
 */
public enum WebSocketTextMessageType {
    /**
     * 유저 초대 알림
     */
    INVITE,

    /**
     * 채팅방 메시지
     */
    NEW_MESSAGE,

    /**
     * 에러발생
     */
    ERROR
}
