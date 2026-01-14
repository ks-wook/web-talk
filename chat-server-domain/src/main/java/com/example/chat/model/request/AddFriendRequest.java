package com.example.chat.model.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record AddFriendRequest (
        @Schema(description = "추가할 친구의 닉네임")
        String friendNickname
) { }
