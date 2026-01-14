package com.example.chat.model.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateStatusTextRequest (
        @Schema(description = "새로운 상태 메시지")
        String statusText
){ }
