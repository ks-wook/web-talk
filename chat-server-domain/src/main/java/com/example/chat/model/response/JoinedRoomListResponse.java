package com.example.chat.model.response;

import com.example.chat.common.exception.ErrorCode;
import com.example.chat.dto.RoomDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 참여중인 채팅방 목록 응답
 */
@Schema(description = "참여중인 채팅방 목록 응답")
public record JoinedRoomListResponse (
        @Schema(description = "결과 값")
        ErrorCode result,

        @Schema(description = "참여중인 채팅방 목록")
        List<RoomDto> roomList
 ) {}
