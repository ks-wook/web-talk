package com.example.OnlineOpenChat.common.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode implements CodeInterface {
    SUCCESS(0, "SUCCESS"),

    // -1 ~ 유저 정보 인증 관련
    USER_ALREADY_EXISTS(-1, "USER_ALREADY_EXISTS"),
    USER_SAVED_FAILED(-2, "USER_SAVED_FAILED"),
    NOT_EXIST_USER(-3, "NOT_EXIST_USER"),
    MIS_MATCH_PASSWORD(-4, "MIS_MATCH_PASSWORD"),

    // -200 ~ JWT 인증 관련
    TOKEN_IS_INVALID(-200, "TOKEN_IS_INVALID"),
    TOKEN_IS_EXPIRED(-201, "TOKEN_IS_EXPIRED"),
    ACCESS_TOKEN_IS_NOT_EXPIRED(-202, "ACCESS_TOKEN_IS_NOT_EXPIRED");

    private final Integer code;
    private final String message;
}
