package com.example.chat.model.response;

import com.example.chat.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;

public record LogoutResponse(
        @Schema(description = "로그아웃 결과 메시지")
        ErrorCode result
) { }
