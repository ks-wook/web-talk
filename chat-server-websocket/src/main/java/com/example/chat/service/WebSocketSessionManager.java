package com.example.chat.service;

import com.example.chat.WebSocketTextMessageType;
import com.example.chat.dto.WebSocketTextMessage;
import com.example.chat.redis.RedisMessageBroker;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

@Service
public class WebSocketSessionManager {

    private static final Logger logger =
            LoggerFactory.getLogger(WebSocketSessionManager.class);

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final RedisMessageBroker redisMessageBroker;
    private final RoomManager roomManager;
    private final WebSocketSendQueue webSocketSendQueue;

    /**
     * userId -> WebSocketSession Set
     * 왜 value가 Set인가?
     * -> 하나의 유저가 여러 디바이스(브라우저 탭 등)에서 접속할 수 있기 때문
     */
    private final ConcurrentMap<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();


    /**
     * 메시지 타입별 핸들러
     */
    private final ConcurrentMap<WebSocketTextMessageType, Consumer<WebSocketTextMessage>> messageHandlers = new ConcurrentHashMap<>();

    private static final String SERVER_ROOMS_KEY_PREFIX = "chat:server:rooms";

    public WebSocketSessionManager(
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper,
            RedisMessageBroker redisMessageBroker,
            RoomManager roomManager,
            WebSocketSendQueue webSocketSendQueue
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.redisMessageBroker = redisMessageBroker;
        this.roomManager = roomManager;
        this.webSocketSendQueue = webSocketSendQueue;
    }

    @PostConstruct
    public void initialize() {
        redisMessageBroker.setLocalMessageHandler(this::handleMessage);
        
        // 메시지 타입별 핸들러 등록
        messageHandlers.put(WebSocketTextMessageType.NEW_MESSAGE, this::newMessageHandler);
        messageHandlers.put(WebSocketTextMessageType.INVITE, this::inviteMessageHandler);
    }

    /**
     * 유저 세션 추가
     * @param userId
     * @param session
     */
    public void addSession(Long userId, WebSocketSession session) {
        logger.info("Adding session {} to server", userId);

        // userId에 해당하는 세션 Set이 없으면 새로 생성 후 추가
        // userId에 해당하는 세션 Set이 이미 있으면 해당 Set에 세션 추가
        userSessions
                .computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet())
                .add(session);
    }

    /**
     * 유저 세션 제거
     * @param userId
     * @param session
     */
    public void removeSession(Long userId, WebSocketSession session) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.remove(session);

            if (sessions.isEmpty()) {
                userSessions.remove(userId);
                logger.info("[WebSocketSessionManager] Removed all sessions for user {}", userId);

                // RoomManager에서도 해당 유저의 세션 정보를 모두 제거
                roomManager.removeSession(session, this::onRoomEmpty);
            }
        }
    }

    /**
     * 해당 채팅 서버 인스턴스가 특정 채팅방 구독 처리
     * @param userId
     * @param roomId
     */
    public void joinRoom(Long userId, Long roomId, WebSocketSession session) {
        String serverId = redisMessageBroker.getServerId();
        String serverRoomKey = SERVER_ROOMS_KEY_PREFIX + serverId;

        Boolean isMember =
                redisTemplate.opsForSet().isMember(serverRoomKey, roomId.toString());

        boolean wasAlreadySubscribed = Boolean.TRUE.equals(isMember);

        if (!wasAlreadySubscribed) {
            redisMessageBroker.subscribeToRoom(roomId);
        }

        redisTemplate.opsForSet().add(serverRoomKey, roomId.toString());

        // RoomManager에 유저 세션 추가
        roomManager.joinRoom(roomId, userId, session);

        logger.info(
                "[WebSocketSessionManager] Joined {} for {} {} to server {}",
                roomId,
                userId,
                serverId,
                serverRoomKey
        );
    }

    /**
     * 해당 채팅 서버 인스턴스가 특정 채팅방 구독 해제 처리
     * @param roomId
     */
    public void onRoomEmpty(Long roomId) {
        String serverId = redisMessageBroker.getServerId();
        String serverRoomKey = SERVER_ROOMS_KEY_PREFIX + serverId;

        // Redis Set에서 해당 방 ID 제거
        redisTemplate.opsForSet().remove(serverRoomKey, roomId.toString());

        // 메시지 브로커 구독 해제
        redisMessageBroker.unsubscribeFromRoom(roomId);

        logger.info(
                "[WebSocketSessionManager] Left {} from server {}",
                roomId,
                serverRoomKey
        );
    }

    /**
     * 메시지 브로커로부터 받은 메시지를 처리하는 로직
     * @param roomId
     * @param message
     */
    public void handleMessage(
            Long roomId,
            WebSocketTextMessage message
    ) {
        Objects.requireNonNull(
                messageHandlers.get(message.getType()),
                () -> "[WebSocketSessionManager] 핸들러가 등록되지 않은 메시지 타입입니다. " + message
        ).accept(message);
    }


    /**
     * 채팅방 메시지에 대한 처리
     * @param message
     */
    public void newMessageHandler(
            WebSocketTextMessage message
    ) {
        try {

            String json = objectMapper.writeValueAsString(message);

            for(WebSocketSession s : roomManager.getSessions(message.getRoomId())) {
                // 열려있는 세션 대상으로 메시지 전송
                if (s.isOpen()) {
                    try {
                        webSocketSendQueue.enqueue(s, new TextMessage(json));
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 초대 알림에 대한 처리
     * @param message
     */
    public void inviteMessageHandler(
            WebSocketTextMessage message
    ) {
        try {

            for(Long targetUserId : message.getTargetUserIds()) {
                Set<WebSocketSession> sessions = userSessions.get(targetUserId);
                if (sessions != null && !sessions.isEmpty()) {

                    for(WebSocketSession s : sessions) {
                        // 열려있는 세션 대상으로 채팅방 초대 메시지 전송
                        if (s.isOpen()) {

                            try {
                                // 1( roomManager에 유저 세션 추가
                                this.joinRoom(targetUserId, message.getRoomId(), s);
                                logger.info("[WebSocketSessionManager] User {} joined room {} due to invite", targetUserId, message.getRoomId());
                                
                                String json = objectMapper.writeValueAsString(message);

                                // 2) 초대 메시지 발송
                                webSocketSendQueue.enqueue(s, new TextMessage(json));
                                logger.info("[WebSocketSessionManager] Sending invite message to user {} for room {}", targetUserId, message.getRoomId());

                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                        }
                    }

                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
