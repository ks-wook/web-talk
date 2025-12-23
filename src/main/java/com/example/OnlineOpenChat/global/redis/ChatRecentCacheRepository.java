package com.example.OnlineOpenChat.global.redis;

import com.example.OnlineOpenChat.domain.chat.mongo.document.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatRecentCacheRepository {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String getRecentKey(Long roomId) {
        return "chat:room:" + roomId + ":recent";
    }

    /*
    public void addToRecent(ChatMessage message) {
        try {
            String key = getRecentKey(message.getRoom().getId());
            String json = objectMapper.writeValueAsString(message);

            redisTemplate.opsForList().rightPush(key, json);

            // 최근 200개 유지
            redisTemplate.opsForList().trim(key, -200, -1);

        } catch (Exception e) {
            throw new RuntimeException("Recent Cache 저장 실패", e);
        }
    }
    */


    public List<ChatMessage> getRecent(Long roomId, int limit) {
        String key = getRecentKey(roomId);

        List<String> cached = redisTemplate.opsForList().range(key, -limit, -1);
        if (cached == null) return List.of();

        return cached.stream().map(s -> {
            try { return objectMapper.readValue(s, ChatMessage.class); }
            catch (Exception e) { return null; }
        }).toList();
    }
}
