package com.example.OnlineOpenChat.global.redis;

import com.example.OnlineOpenChat.domain.chat.model.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ChatStreamRepository {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String getStreamKey(Long roomId) {
        return "chat:room:" + roomId + ":stream";
    }

    /**
     * 스트림에 메시지 밀어넣는 함수
     * @param message
     */
    public void addToStream(ChatMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);

            Map<String, String> map = Map.of("data", json);

            RecordId recordId = redisTemplate.opsForStream().add(
                    StreamRecords.mapBacked(map)
                            .withStreamKey(getStreamKey(message.getRoomId()))
            );

        } catch (Exception e) {
            throw new RuntimeException("Stream 저장 실패", e);
        }
    }
}
