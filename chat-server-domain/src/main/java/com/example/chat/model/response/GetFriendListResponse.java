package com.example.chat.model.response;

import com.example.chat.common.exception.ErrorCode;
import com.example.chat.dto.FriendDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record GetFriendListResponse (
        @Schema(description = "결과")
        ErrorCode result,

        @Schema(description = "친구 목록")
        List<FriendDto> friendList
) { }
