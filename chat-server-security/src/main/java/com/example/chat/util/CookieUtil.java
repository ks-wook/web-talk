package com.example.chat.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
    public static void addRefreshTokenCookie(HttpServletResponse response, String refreshToken, Date refreshTokenExpiredAt) {
        long expireTimeMs = refreshTokenExpiredAt.getTime();
        long nowMs = System.currentTimeMillis();

        int maxAge = (int) ((expireTimeMs - nowMs) / 1000);

        Cookie cookie = new Cookie("onlineOpenChatRefresh", refreshToken);

        cookie.setHttpOnly(true);               // 프론트 스크립트에서 접근 불가 → XSS 보호
        cookie.setSecure(false);                // 개발 테스트를 위해 http 허용
        cookie.setPath("/");                    // 전체 경로에서 유효
        cookie.setMaxAge(maxAge);  // 14일

        response.addCookie(cookie);
    }

    /**
     * RefreshToken 쿠키 삭제
     * @param response
     */
    public static void deleteRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("onlineOpenChatRefresh", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 즉시 만료

        response.addCookie(cookie);
    }
}