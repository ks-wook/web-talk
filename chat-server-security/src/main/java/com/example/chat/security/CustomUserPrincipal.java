package com.example.chat.security;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 인증된 유저 세팅 값
 */
@Data
@AllArgsConstructor
public class CustomUserPrincipal {
    private String UserId;
}
