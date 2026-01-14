package com.example.chat.model.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateNicknameRequest(
    @Schema(description = "새 닉네임")
    String newNickname
) { }
