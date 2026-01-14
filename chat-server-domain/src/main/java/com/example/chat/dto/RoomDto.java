package com.example.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 프론트엔드로 전달할 친구 dto 값
 * @param id
 * @param name
 */
public record RoomDto(
        @Schema(description = "방 id")
        Long id,

        @Schema(description = "채팅방 명")
        String name
) {}