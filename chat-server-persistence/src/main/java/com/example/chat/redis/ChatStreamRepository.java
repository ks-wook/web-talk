package com.example.chat.redis;

import com.example.chat.dto.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

import static com.example.chat.redis.constants.Constants.CHAT_STREAM_KEY;

@Repository
@RequiredArgsConstructor
public class ChatStreamRepository {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /*
    private String getStreamKey(Long roomId) {
        return "chat:room:" + roomId + ":stream";
    }
     */

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
                            .withStreamKey(CHAT_STREAM_KEY) // roomId를 기반으로 스트림 키 생성
            );

        } catch (Exception e) {
            throw new RuntimeException("Stream 저장 실패", e);
        }
    }
}
