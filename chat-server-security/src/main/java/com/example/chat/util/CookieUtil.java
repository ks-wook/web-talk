package com.example.chat.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import java.util.Date;

/**
 * 쿠키 발급 관련 유틸 함수
 */
public class CookieUtil {

    /**
     * Request 에서 RefreshToken 쿠키 추출
     * @param request
     * @return
     */
    public static String getRefreshTokenFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("onlineOpenChatRefresh".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * RefreshToken 발급 처리
     */
    public static void addRefreshTokenCookie(
            HttpServletResponse response,
            String refreshToken,
            Date refreshTokenExpiredAt
    ) {
        long expireTimeMs = refreshTokenExpiredAt.getTime();
        long nowMs = System.currentTimeMillis();

        long maxAgeSeconds = (expireTimeMs - nowMs) / 1000;

        ResponseCookie cookie = ResponseCookie.from("onlineOpenChatRefresh", refreshToken)
                .httpOnly(true)
                .secure(true)                 // HTTPS 필수
                .path("/")
                .sameSite("None")             // 핵심
                .maxAge(maxAgeSeconds)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * RefreshToken 쿠키 삭제
     * @param response
     */
    public static void deleteRefreshTokenCookie(HttpServletResponse response) {

        ResponseCookie cookie = ResponseCookie.from("onlineOpenChatRefresh", "")
                .httpOnly(true)
                .secure(true)          // 생성 시와 동일
                .sameSite("None")      // 생성 시와 동일
                .path("/")
                .maxAge(0)             // 즉시 만료
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}