package com.example.chat.redis;

import com.example.chat.dto.WebSocketTextMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

@Slf4j
@Service
public class RedisMessageBroker implements MessageListener {

    private static final Logger logger =
            LoggerFactory.getLogger(RedisMessageBroker.class);

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisMessageListenerContainer messageListenerContainer;
    private final ObjectMapper objectMapper;
    private final ChannelTopic notificationTopic;

    /**
     * 서버 고유 ID값
     */
    @Getter
    private final String serverId =
            Optional.ofNullable(System.getenv("HOSTNAME"))
                    .orElse("server-" + System.currentTimeMillis());

    /**
     * 구독중인 방 목록
     */
    private final Set<Long> subscribeRooms =
            ConcurrentHashMap.newKeySet();

    /**
     * local handler (roomId, ChatMessage)
     */
    @Setter
    private volatile BiConsumer<Long, WebSocketTextMessage> localMessageHandler;

    public RedisMessageBroker(
            RedisTemplate<String, String> redisTemplate,
            RedisMessageListenerContainer messageListenerContainer,
            ObjectMapper objectMapper,
            @Qualifier ("notificationTopic") ChannelTopic notificationTopic
    ) {
        this.redisTemplate = redisTemplate;
        this.messageListenerContainer = messageListenerContainer;
        this.objectMapper = objectMapper;
        this.notificationTopic = notificationTopic;
    }

    @PreDestroy
    public void cleanup() {

        /**
         * 구독했던 방들 구독 해지
         */
        for (Long roomId : subscribeRooms) {
            unsubscribeFromRoom(roomId);
        }
        logger.info("[RedisMessageBroker] Removing RedisMessageListenerContainer");
    }

    /**
     * 알림 채널 구독
     */
    @PostConstruct
    public void subscribeToNotificationChannel() {
        messageListenerContainer.addMessageListener(this, notificationTopic);
        logger.info("[RedisMessageBroker] Subscribed to notification channel");
    }

    /**
     * 특정 방 구독
     */
    public void subscribeToRoom(Long roomId) {
        if (subscribeRooms.add(roomId)) {
            ChannelTopic topic = new ChannelTopic("chat.room." + roomId);
            messageListenerContainer.addMessageListener(this, topic);
            logger.info("Subscribed to {}", roomId);
        } else {
            logger.error("[RedisMessageBroker] Room {} does not exist", roomId);
        }
    }

    /**
     * 특정 방 구독 해제
     */
    public void unsubscribeFromRoom(Long roomId) {
        if (subscribeRooms.remove(roomId)) {
            ChannelTopic topic = new ChannelTopic("chat.room." + roomId);
            messageListenerContainer.removeMessageListener(this, topic);
            logger.info("Unsubscribed from {}", roomId);
        } else {
            logger.error("[RedisMessageBroker] Room {} does not exist", roomId);
        }
    }

    /**
     * Redis를 통해 다른 인스턴스로 메시지 전파
     */
    public void broadcastToRoom(Long roomId, WebSocketTextMessage payload) {

        // 특정 방 채널토픽으로 publish
        try {
            String json = objectMapper.writeValueAsString(payload);
            redisTemplate.convertAndSend("chat.room." + roomId, json);

            logger.info("[RedisMessageBroker] Broadcast to {} : {}", roomId, json);

        } catch (Exception e) {
            logger.error("[RedisMessageBroker] Error broadcast to {}", roomId, e);
        }
    }

    /**
     * subscribed 채널로부터 메시지 수신 처리
     * @param message message must not be {@literal null}.
     * @param pattern pattern matching the channel (if specified) - can be {@literal null}.
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String json = new String(message.getBody(), StandardCharsets.UTF_8);
            WebSocketTextMessage webSocketTextMessage = objectMapper.readValue(json, WebSocketTextMessage.class);

            if (localMessageHandler != null) {
                localMessageHandler.accept(
                        webSocketTextMessage.getRoomId(),
                        webSocketTextMessage
                );
            }
            else {
                log.error("[RedisMessageBroker] Invalid message Type: message -> {}", webSocketTextMessage);
            }

        } catch (Exception e) {
            logger.error("Error in on message", e);
        }
    }

    /**
     * 메시지 브로커(Redis)로 알림 메시지 전송
     * @param payload
     */
    public void broadcastNotification (WebSocketTextMessage payload) {

        try {
            String json = objectMapper.writeValueAsString(payload);
            redisTemplate.convertAndSend(notificationTopic.getTopic(), json);

        } catch (Exception e) {
            logger.error("Error broadcast notification", e);
        }
    }
}
