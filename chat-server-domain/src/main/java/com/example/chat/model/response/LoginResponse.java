package com.example.chat.model.response;


import com.example.chat.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;

@Schema(description = "로그인 요청 응답")
public record LoginResponse (

        @Schema(description = "error code")
        ErrorCode result,

        @Schema(description = "jwt token")
        @Nullable
        String token,

        @Schema(description = "닉네임")
        String nickname
) {}
