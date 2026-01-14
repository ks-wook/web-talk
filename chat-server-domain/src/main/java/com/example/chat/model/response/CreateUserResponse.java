package com.example.chat.model.response;


import com.example.chat.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;

@Schema(description = "유저 계정 생성 응답")
public record CreateUserResponse (
        @Schema(description = "error code")
        ErrorCode result,

        @Schema(description = "생성된 유저명")
        @Nullable
        String userId
) {}
