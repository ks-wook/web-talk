package com.example.chat.model.response;

import com.example.chat.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;

public record ReissueAccessTokenResponse(
    @Schema(description = "결과")
    ErrorCode result,

    @Schema(description = "재발급된 액세스 토큰")
    String accessToken
)
{ }
