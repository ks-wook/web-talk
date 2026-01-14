package com.example.chat.model.response;

import com.example.chat.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateStatusTextResponse(
    @Schema(description = "상태 메시지 업데이트 결과")
    ErrorCode result,

    @Schema(description = "업데이트된 상태 메시지")
    String statusText
) { }
