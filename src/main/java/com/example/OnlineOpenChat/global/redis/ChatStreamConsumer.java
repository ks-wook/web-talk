package com.example.OnlineOpenChat.global.redis;

import com.example.OnlineOpenChat.domain.chat.model.ChatMessage;
import com.example.OnlineOpenChat.domain.repository.ChatRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatStreamConsumer {

    private final StringRedisTemplate redisTemplate;
    private final ChatRepository chatMessageRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static final String CONSUMER;

    static {
        String host;
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            host = "unknown-host";
        }
        CONSUMER = host + "-" + UUID.randomUUID();
        log.info("This instance Consumer ID = {}", CONSUMER);
    }

    /**
     * redis Stream 생성
     */
    @PostConstruct
    public void createGroup() {
        String key = "chat:room:1:stream";
        String group = "chat-group";

        try {
            // 1) Stream 생성: dummy 메시지 추가 -> 스트림을 열기위한 용도
            Map<String, String> initBody = Map.of("data", "init");

            RecordId dummyId = redisTemplate.opsForStream().add(
                    StreamRecords.mapBacked(initBody).withStreamKey(key)
            );

            // 2) Consumer Group 생성
            redisTemplate.opsForStream().createGroup(key, group);

            // 3) dummy 메시지 제거
            redisTemplate.opsForStream().delete(key, dummyId);

            log.info("Stream + ConsumerGroup initialized: {}", group);




        } catch (Exception e) {
            log.info("Consumer Group already exists or Stream already initialized");
        }
    }

    /**
     * 채팅 로그 데이터 소비 (가져와서 DB에 기록)
     */
    @Scheduled(fixedDelay = 300)
    public void consume(MapRecord<String, String, String> record) {
        try {
            String json = record.getValue().get("data");
            ChatMessage msg = objectMapper.readValue(json, ChatMessage.class);

            log.info("[STREAM] consumed: {}", msg);

        } catch (Exception e) {
            log.error("Stream consume error", e);
        }
    }
}
