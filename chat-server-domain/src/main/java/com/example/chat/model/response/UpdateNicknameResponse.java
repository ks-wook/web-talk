package com.example.chat.model.response;

import com.example.chat.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateNicknameResponse(
    @Schema(description = "결과")
    ErrorCode result,

    @Schema(description = "변경된 닉네임")
    String changedNickname
) { }
