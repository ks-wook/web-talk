package com.example.chat.config;

import com.example.chat.redis.ChatStreamConsumer;
import com.example.chat.redis.constants.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;
import java.util.UUID;

@Slf4j
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

        if (redisConnectionFactory instanceof LettuceConnectionFactory lettuce) {
            RedisStandaloneConfiguration config =
                    (RedisStandaloneConfiguration) lettuce.getStandaloneConfiguration();

            log.info(
                    "[RedisStreamConfig] Connected Redis -> host: {}, port: {}",
                    config.getHostName(),
                    config.getPort()
            );
        } else {
            log.warn(
                    "[RedisStreamConfig] RedisConnectionFactory is not LettuceConnectionFactory: {}",
                    redisConnectionFactory.getClass().getName()
            );
        }

        // 로그성 이벤트 이므로 autoAck 사용하여 처리 -> 채팅 등의 실시간성 서비스에 적합
        container.receiveAutoAck(
                Consumer.from(Constants.CHAT_CONSUMER_GROUP, generateConsumerName()),
                StreamOffset.create(Constants.CHAT_STREAM_KEY, ReadOffset.lastConsumed()),
                chatStreamConsumer::consume
        );

        container.start();
        return container;
    }

    private String generateConsumerName() {
        return "consumer-" + UUID.randomUUID();
    }
}

