package com.example.OnlineOpenChat.security;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.OnlineOpenChat.common.Constants.Constants;
import com.example.OnlineOpenChat.common.exception.CustomException;
import com.example.OnlineOpenChat.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;


@Slf4j
@Component
public class JWTProvider {
    private static String secreteKey;
    private static String refreshSecretKey;
    private static long tokenTimeForMinute;
    private static long refreshTokenTimeForMinute;

    @Value("${token.secret-key}")
    public void setSecreteKey(String secreteKey) {
        JWTProvider.secreteKey = secreteKey;
    }

    @Value("${token.refresh-secret-key}")
    public void setRefreshSecretKey(String refreshSecretKey) {
        JWTProvider.refreshSecretKey = refreshSecretKey;
    }

    @Value("${token.token-time}")
    public void setTokenTimeForMinute(long tokenTime) {
        JWTProvider.tokenTimeForMinute = tokenTime;
    }

    @Value("${refresh-token-time}")
    public void setRefreshTokenTimeForMinute(long refreshTokenTime) {
        JWTProvider.refreshTokenTimeForMinute = refreshTokenTime;
    }

    /**
     * JWT 액세스 토큰 발급
     * @param name
     * @return
     */
    public static String createToken(String name) {
        return JWT.create()
                .withSubject(name)
                .withIssuedAt(new Date(System.currentTimeMillis() + tokenTimeForMinute * Constants.ON_MINUTE_TO_MILLIS))
                .sign(Algorithm.HMAC256(secreteKey));

    }

    /**
     * JWT Refresh 토큰 발급
     * @param name
     * @return
     */
    public static String createRefreshToken(String name) {
        return JWT.create()
                .withSubject(name)
                .withIssuedAt(new Date(System.currentTimeMillis() + refreshTokenTimeForMinute * Constants.ON_MINUTE_TO_MILLIS))
                .sign(Algorithm.HMAC256(refreshSecretKey));

    }

    /**
     * 토큰이 만료되었는지 검증
     * @param token
     * @return
     */
    public static DecodedJWT checkTokenForRefresh(String token) {
        try {
            DecodedJWT decoded = JWT.require(Algorithm.HMAC256(secreteKey)).build().verify(token);
            log.error("token must be expired : {}", decoded.getSubject());
            throw new CustomException(ErrorCode.ACCESS_TOKEN_IS_NOT_EXPIRED);
        } catch(AlgorithmMismatchException | SignatureVerificationException | InvalidClaimException e) {
            throw new CustomException((ErrorCode.TOKEN_IS_INVALID));
        } catch (TokenExpiredException e) {
            return JWT.decode(token);
        }
    }

    public static DecodedJWT decodeAccessToken(String token) {
        return decodeTokenAfterVerify(token, secreteKey);
    }

    public static DecodedJWT decodeRefreshToken(String token) {
        return decodeTokenAfterVerify(token, refreshSecretKey);
    }

    /**
     * 인증된 토큰에 대해 decoding된 값 반환
     * @param token
     * @param key
     * @return
     */
    public static DecodedJWT decodeTokenAfterVerify(String token, String key) {
        try {
            return JWT.require(Algorithm.HMAC256(key)).build().verify(token);
        } catch(AlgorithmMismatchException | SignatureVerificationException | InvalidClaimException e) {
            throw new CustomException((ErrorCode.TOKEN_IS_INVALID));
        } catch (TokenExpiredException e) {
            return JWT.decode(token);
        }
    }

    /**
     * JWT 복호화
     * @param token
     * @return
     */
    public static DecodedJWT decodedJWT(String token) {
        return JWT.decode((token));
    }

    /**
     * 토큰에서 유저명 추출
     * @param token
     * @return
     */
    public static String getUserFromToken(String token) {
        DecodedJWT jwt = decodedJWT((token));
        return jwt.getSubject();
    }
}