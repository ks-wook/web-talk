package com.example.chat.model.response;

import com.example.chat.common.exception.ErrorCode;
import com.example.chat.dto.ChatMessage;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.*;

@Schema(description = "Chatting List")
public record ChatListResponse(
        @Schema(description = "결과")
        ErrorCode result,

        @Schema(description = "채팅방의 최근 채팅 내역")
        List<ChatMessage> messages
) {}
