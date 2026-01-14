package com.example.chat.model.response;

import com.example.chat.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "채팅 방 생성 응답")
public record CreateRoomResponse (
        @Schema(description = "결과")
        ErrorCode result,

        @Schema (description = "생성된 방 이름")
        String roomName,

        @Schema(description = "생성된 방 ID")
        Long roomId
) { }
