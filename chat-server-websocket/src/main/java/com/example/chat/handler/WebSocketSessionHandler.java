package com.example.chat.handler;

import com.example.chat.dto.WebSocketTextMessage;
import com.example.chat.service.ChatMessageService;
import com.example.chat.WebSocketTextMessageType;
import com.example.chat.service.ChatService;
import com.example.chat.service.WebSocketSessionManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class WebSocketSessionHandler implements WebSocketHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(WebSocketSessionHandler.class);

    private final WebSocketSessionManager sessionManager;
    private final ChatMessageService chatMessageService;
    private final ChatService chatService;
    private final ObjectMapper objectMapper;


    /**
     * 웹소켓 세션 연결 성공시 처리
     * @param session
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = getUserIdFromSession(session);

        if (userId != null) {
            sessionManager.addSession(userId, session);
            logger.info("Session {} established for {}", userId, userId);

            try {
                loadUserChatRooms(userId, session);
            } catch (Exception e) {
                logger.error("Error while loading user chat rooms", e);
            }


        }
    }

    /**
     * 웹소켓 세션으로부터 메시지 수신시
     * @param session
     * @param message
     */
    @Override
    public void handleMessage(
            WebSocketSession session,
            WebSocketMessage<?> message
    ) {
        Long userId = getUserIdFromSession(session);
        if (userId == null) {
            log.error("유효하지 않은 세션에서 수신된 메시지 입니다. : message={}", message.getPayload());
            return;
        }

        try {
            if (message instanceof TextMessage textMessage) {
                // 메시지 처리
                onReceived(session, userId, textMessage.getPayload());

            } else {
                logger.warn("Unsupported message type {}", message.getClass().getName());
            }

        } catch (Exception e) {
            logger.warn("exception while processing message", e);

            // 오류 발생시 클라이언트에 에러메시지 전송
            sendErrorMessage(session, "메시지 처리 에러", null);
        }
    }

    @Override
    public void handleTransportError(
            WebSocketSession session,
            Throwable exception
    ) {
        Long userId = getUserIdFromSession(session);

        // EOFException → 정상적인 클라이언트 연결 종료
        if (exception instanceof java.io.EOFException) {
            logger.debug("WebSocket connection closed by client for user: {}", userId);
        } else {
            logger.error("WebSocket transport error for user: {}", userId, exception);
        }

        if (userId != null) {
            // roomManager과 sessionManager에서 세션 제거
            onDisconnected(session, userId);
        }
    }

    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus closeStatus
    ) {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            // roomManager과 sessionManager에서 세션 제거
            onDisconnected(session, userId);

            logger.info("Session Disconnected for {}", userId);
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private Long getUserIdFromSession(WebSocketSession session) {
        Map<String, Object> attributes = session.getAttributes();
        Object userId = attributes.get("userId");
        return userId instanceof Long ? (Long) userId : null;
    }

    /**
     * 세션 연결 종료시 처리
     * @param session
     * @param userId
     */
    private void onDisconnected(WebSocketSession session, Long userId) {
        // roomManager과 sessionManager에서 세션 제거
        sessionManager.removeSession(userId, session);
        logger.info("Session Disconnected for {}", userId);
    }

    /**
     * 유저가 속해있던 채팅방 목록 세팅
     * @param userId
     */
    private void loadUserChatRooms(Long userId, WebSocketSession session) {
        try {
            // 유저가 속한 채팅방 목록을 가져와서 세션 매니저에 등록
            var chatRooms = chatService.findRoomsByUserId(userId);

            for (var chatRoom : chatRooms) {
                sessionManager.joinRoom(userId, chatRoom.id(), session);
            }

            /*
            // RoomManager에 유저가 속한 채팅방 목록 세팅
            roomManager.joinRooms(
                    chatRooms.stream()
                            .map(RoomDto::id)
                            .collect(Collectors.toSet()),
                    userId,
                    session
            );
            */

            logger.info(
                    "Loaded {} chat rooms for user: {}",
                    chatRooms.size(),
                    userId
            );

        } catch (Exception e) {
            logger.error("Failed to load chat rooms for user: {}", userId, e);
        }
    }

    /**
     * 에러 메시지 전달
     * @param session
     * @param errorMessage
     * @param errorCode
     */
    private void sendErrorMessage(
            WebSocketSession session,
            String errorMessage,
            String errorCode
    ) {
        try {
            WebSocketTextMessage errorMsg = WebSocketTextMessage.builder()
                    .type(WebSocketTextMessageType.ERROR)
                    .message(errorMessage)
                    .build();

            String json = objectMapper.writeValueAsString(errorMsg);
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            logger.error("Failed to send error message", e);
        }
    }

    private String extractMessageType(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            JsonNode typeNode = root.get("type");
            return typeNode != null ? typeNode.asText() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 메시지 수신시 처리
     * @param session
     * @param userId
     * @param payload
     */
    private void onReceived(
            WebSocketSession session,
            Long userId,
            String payload
    ) {
        try {
            // String messageType = extractMessageType(payload);

            log.info("WebSocket message received from user {}: {}", userId, payload);

            WebSocketTextMessage webSocketChatMessage
                    = objectMapper.readValue(payload, WebSocketTextMessage.class);

            // 메시지 유효성 검사
            if(webSocketChatMessage.getType() == null) {
                throw new IllegalArgumentException("타입이 지정되지않은 메시지 입니다.");
            }
            
            // redisMessageBroker에게 메시지 전달
            chatMessageService.sendMessage(session, userId, webSocketChatMessage);

        } catch (Exception e) {
            logger.error(
                    "Error parsing WebSocket message from user {}: {}",
                    userId,
                    e.getMessage(),
                    e
            );
            sendErrorMessage(
                    session,
                    "메시지 형식이 올바르지 않습니다.",
                    "INVALID_MESSAGE_FORMAT"
            );
        }
    }
}
