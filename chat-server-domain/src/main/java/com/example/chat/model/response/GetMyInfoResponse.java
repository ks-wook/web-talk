package com.example.chat.model.response;

import com.example.chat.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;

public record GetMyInfoResponse (
        @Schema(description = "결과")
        ErrorCode result,

        @Schema(description = "로그인된 유저의 Id 값")
        Long userId,

        @Schema(description = "로그인된 유저의 닉네임")
        String nickname,

        @Schema(description = "로그인된 유저의 상태메시지")
        String statusText
){ }
