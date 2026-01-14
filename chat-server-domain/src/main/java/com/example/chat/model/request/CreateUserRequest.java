package com.example.chat.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "유저 계정 생성 요청")
public record CreateUserRequest (
    @Schema(description = "로그인 아이디")
    @NotBlank
    @NotNull
    String loginId,

    @Schema(description = "유저 비밀번호")
    @NotBlank
    @NotNull
    String password
) {}
