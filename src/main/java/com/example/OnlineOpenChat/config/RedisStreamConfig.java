package com.example.OnlineOpenChat.config;

import com.example.OnlineOpenChat.global.redis.ChatStreamConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class RedisStreamConfig {

    private final RedisConnectionFactory redisConnectionFactory;
    private final ChatStreamConsumer chatStreamConsumer;

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>>
    chatStreamListenerContainer() {

        var options = StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                .<String, MapRecord<String, String, String>>builder()
                .pollTimeout(Duration.ofMillis(500))
                .build();

        var container = StreamMessageListenerContainer.create(redisConnectionFactory, options);

        // 로그성 이벤트 이므로 autoAck 사용하여 처리 -> 채팅 등의 실시간성 서비스에 적합
        container.receiveAutoAck(
                Consumer.from("chat-group", generateConsumerName()),
                StreamOffset.create("chat:room:1:stream", ReadOffset.lastConsumed()),
                chatStreamConsumer::consume
        );

        container.start();
        return container;
    }

    private String generateConsumerName() {
        return "consumer-" + UUID.randomUUID();
    }
}

