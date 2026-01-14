package com.example.chat.repository;

import com.example.chat.dto.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository
        extends MongoRepository<ChatMessage, String> {

    List<ChatMessage> findTop100ByRoomIdOrderBySentAtAsc(Long roomId);
}