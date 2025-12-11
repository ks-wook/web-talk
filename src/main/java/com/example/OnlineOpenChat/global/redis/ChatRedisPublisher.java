package com.example.OnlineOpenChat.global.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

/**
 * 채팅 메시지 퍼블리셔 - Redis(메시지브로커) 메시지 전달
 */
@Component
@RequiredArgsConstructor
public class ChatRedisPublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic chatTopic;

    /**
     * 메시지 브로커(Redis)로 메시지 전송
     * @param message
     */
    public void publish(Object message) {
        redisTemplate.convertAndSend(chatTopic.getTopic(), message);
    }
}
