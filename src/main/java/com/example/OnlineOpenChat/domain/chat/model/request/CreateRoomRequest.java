package com.example.OnlineOpenChat.domain.chat.model.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 채팅방 생성 요청
 */
@Schema(description = "채팅방 생성 요청")
public record CreateRoomRequest (
        @Schema(description = "채팅방 이름")
        String roomName,

        @Schema(description = "채팅방 참여 유저 ID 리스트")
        List<Long> participantIds
) { }
