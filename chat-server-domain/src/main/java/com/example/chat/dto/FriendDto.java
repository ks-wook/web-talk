package com.example.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 프론트엔드로 전달할 친구 dto 값
 * @param id
 * @param nickname
 * @param statusText
 */
public record FriendDto(
        @Schema(description = "친구 ID")
        Long id,

        @Schema(description = "친구 닉네임")
        String nickname,

        @Schema(description = "상태 메시지")
        String statusText
) {}