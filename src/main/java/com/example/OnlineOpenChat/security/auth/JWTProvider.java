package com.example.OnlineOpenChat.security.auth;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.OnlineOpenChat.common.Constants.Constants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JWTProvider {
    private static String secreteKey;
    // private static String refreshSecretKey;

    // accessToken 만료 시간
    private static long tokenTimeForMinute;

    // refreshToken 만료 시간 (일자 기준)
    private static long refreshTokenTimeForDay;



    @Value("${token.secret-key}")
    public void setSecreteKey(String secreteKey) {
        JWTProvider.secreteKey = secreteKey;
    }

    @Value("${token.access-token-time}")
    public void setTokenTimeForMinute(long tokenTime) {
        JWTProvider.tokenTimeForMinute = tokenTime;
    }

    @Value("${token.refresh-token-time-day}")
    public void setRefreshTokenTimeForDay(long refreshTokenTimeForDay) {
        JWTProvider.refreshTokenTimeForDay = refreshTokenTimeForDay;
    }


    /**
     * AccessToken 발급
     * @param userId
     * @return
     */
    public static String createAccessToken(String userId) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + Constants.ON_HOUR_TO_MILLIS * 30); // 30분

        return JWT.create()
                .withSubject(String.valueOf(userId))
                .withIssuedAt(now)
                .withExpiresAt(exp)
                .sign(Algorithm.HMAC256(secreteKey));
    }

    /**
     * RefreshToken 발급 - 로그인 시
     * @param
     * @return
     */
    public static String createRefreshToken() {
        byte[] randomBytes = new byte[64];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * 현재 시간으로부터 refreshToken의 만료 날짜 획득 - 로그인 시 refreshToken 저장을 위해 호출
     */
    public static Date getRefreshTokenExpiredAtFromNow() {
        Date now = new Date();

        // refreshTokenTimeForDay -> 14일, application.yml 파일 참조
        return new Date(now.getTime() + Constants.ON_HOUR_TO_MILLIS * 24 * refreshTokenTimeForDay); // 현재로부터 14일간 유지
    }


    /**
     * 토큰에서 UserId 값 추출
     * @param token
     * @return
     */
    public static String getUserId(String token) {
        DecodedJWT decoded = JWT.require(Algorithm.HMAC256(secreteKey))
                .build()
                .verify(token);
        return decoded.getSubject();
    }

    /**
     * Jwt 유효성 검증
     * @param token
     * @return
     */
    public static boolean validateToken(String token) {
        try {
            JWT.require(Algorithm.HMAC256(secreteKey))
                    .build()
                    .verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Http 요청에서 토큰 추출
     * @param request
     * @return
     */
    public static String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");

        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    /**
     * authorization 헤더에서 토큰값 추출
     * @param header
     * @return
     */
    public static String extractToken(String header) {
        if(StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}