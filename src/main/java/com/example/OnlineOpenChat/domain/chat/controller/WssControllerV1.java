package com.example.OnlineOpenChat.domain.chat.controller;

import com.example.OnlineOpenChat.domain.chat.model.Message;
import com.example.OnlineOpenChat.domain.chat.mongo.document.ChatMessage;
import com.example.OnlineOpenChat.domain.chat.service.ChatServiceV1;
import com.example.OnlineOpenChat.global.redis.RedisMessage;
import com.example.OnlineOpenChat.global.redis.publisher.ChatRedisPublisher;
import com.example.OnlineOpenChat.global.redis.ChatStreamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Controller
@Slf4j
/**
 * 채팅 메시지 처리 컨트롤러
 * 참고문서 : https://docs.spring.io/spring-framework/reference/web/websocket/stomp/handle-annotations.html
 */
public class WssControllerV1 {

    private final ChatServiceV1 chatServiceV1;
    private final ChatRedisPublisher publisher;

    private final ChatStreamRepository chatStreamRepository;


    /**
     * 채팅 메시지 처리
     * @param roomId
     * @param msg
     * @return
     */
    @MessageMapping("/chat/message/{roomId}")
    // @SendTo("/sub/chat")
    public void receivedMessage(
            @DestinationVariable String roomId,
            RedisMessage msg)
    {
        log.info("Message Received -> roomId: {}, from: {}, msg: {}", roomId, msg.getSenderName(), msg.getMessage());

        // 2) Redis Stream으로 실시간 채팅 로그 기록
        ChatMessage chatMessage = ChatMessage.builder()
                .roomId(Long.parseLong(roomId))
                .senderName(msg.getSenderName())
                .message(msg.getMessage())
                .sentAt(System.currentTimeMillis())
                .userId(msg.getUserId())
                .build();

        chatStreamRepository.addToStream(chatMessage);

        // 3) 메시지 브로커에게 메시지 퍼블리싱
        publisher.publish(msg);
    }
}
