package com.example.OnlineOpenChat.domain.auth.model.response;


import com.example.OnlineOpenChat.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "유저 계정 생성 응답")
public record LoginResponse (

        @Schema(description = "error code")
        ErrorCode description,

        @Schema(description = "jwt token")
        String token
) {}
