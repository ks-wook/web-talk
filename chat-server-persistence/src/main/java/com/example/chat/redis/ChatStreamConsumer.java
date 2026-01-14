package com.example.chat.redis;


import com.example.chat.dto.ChatMessage;
import com.example.chat.service.ChatMessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.UUID;

import static com.example.chat.redis.constants.Constants.CHAT_CONSUMER_GROUP;
import static com.example.chat.redis.constants.Constants.CHAT_STREAM_KEY;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatStreamConsumer {

    private final ChatMessageService chatMessageService;

    private final StringRedisTemplate redisTemplate;
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
        try {
            // Consumer Group 생성
            redisTemplate.opsForStream().createGroup(
                    CHAT_STREAM_KEY,
                    ReadOffset.from("0-0"),
                    CHAT_CONSUMER_GROUP
            );

            log.info("Stream + ConsumerGroup initialized: {}", CHAT_CONSUMER_GROUP);

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

            log.info("[STREAM] consuming: {}", json);

            ChatMessage msg = objectMapper.readValue(json, ChatMessage.class);

            // MongoDB 채팅로그 기록
            chatMessageService.saveMessage(msg);

            log.info("[STREAM] consumed: {}", msg);

        } catch (Exception e) {
            log.error("Stream consume error", e);
        }
    }
}
