package com.example.chat.service;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * 웹소켓 SendQueue에 담길 작업 정보 record
 * @param session
 * @param message
 */
public record WebSocketSendJob(
        @Schema(description = "웹소켓 세션")
        WebSocketSession session,

        @Schema(description = "전송할 메시지")
        TextMessage message
) {}