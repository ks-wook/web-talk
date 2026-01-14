package com.example.chat.service;

import com.example.chat.dto.WebSocketTextMessage;
import com.example.chat.redis.ChatStreamRepository;
import com.example.chat.redis.RedisMessageBroker;
import com.example.chat.dto.ChatMessage;
import com.example.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatStreamRepository chatStreamRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final RedisMessageBroker redisMessageBroker;

    /**
     * 채팅 메시지 저장
     * @param roomId
     * @param userId
     * @param message
     */
    public void saveMessage(Long roomId, Long userId, String senderName, String message) {
        ChatMessage chatMessage = ChatMessage.builder()
                .roomId(roomId)
                .userId(userId)
                .senderName(senderName)
                .message(message)
                .sentAt(System.currentTimeMillis())
                .build();

        chatMessageRepository.save(chatMessage);
    }

    public void saveMessage(ChatMessage msg) {
        ChatMessage chatMessage = ChatMessage.builder()
                .roomId(msg.getRoomId())
                .userId(msg.getUserId())
                .senderName(msg.getSenderName())
                .message(msg.getMessage())
                .sentAt(System.currentTimeMillis())
                .build();

        chatMessageRepository.save(chatMessage);
    }

    /**
     * 웹소켓으로부터 받은 메시지를 DB 저장형태로 변환
     * @param msg
     * @return
     */
    public ChatMessage WebSocketTextMessageToChatMessage(WebSocketTextMessage msg) {

        return ChatMessage.builder()
                .roomId(msg.getRoomId())
                .userId(msg.getUserId())
                .senderName(msg.getSenderName())
                .message(msg.getMessage())
                .sentAt(System.currentTimeMillis())
                .build();
    }

    /**
     * 최근 100개의 채팅 메시지 조회
     * @param roomId
     * @return
     */
    public List<ChatMessage> getRecentMessages(Long roomId) {
        return chatMessageRepository
                .findTop100ByRoomIdOrderBySentAtAsc(roomId);
    }

    /**
     * 웹소켓으로부터 수신받은 메시지 전송 처리
     * @param session
     * @param userId
     * @param message
     */
    public void sendMessage(WebSocketSession session, Long userId, WebSocketTextMessage message) {

        // 전송받은 메시지 저장 -> 바로 하지 않고 redisStream에 저장 -> 잠시뒤 Consumer에서 가져가서 처리됨
        // DB에 저장하는 시간을 아끼고 실시간성 보장을 위해
        chatStreamRepository.addToStream(WebSocketTextMessageToChatMessage(message));

        // RedisMessageBroker를 통해 메시지 전파
        redisMessageBroker.broadcastToRoom(message.getRoomId(), message);
    }
}