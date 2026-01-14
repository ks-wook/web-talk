package com.example.chat.service;

import com.example.chat.model.request.CreateUserRequest;
import com.example.chat.model.request.LoginRequest;
import com.example.chat.model.response.CreateUserResponse;
import com.example.chat.model.response.GetMyInfoResponse;
import com.example.chat.model.response.LoginResponse;
import com.example.chat.model.response.LogoutResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    /**
     * 유저 회원 가입 처리
     *
     * @param request 회원가입 요청 정보
     * @return 회원가입 결과
     */
    CreateUserResponse createUser(CreateUserRequest request);

    /**
     * 유저 로그인 처리
     *
     * @param request  로그인 요청 정보
     * @param response HTTP 응답 (RefreshToken 쿠키 세팅용)
     * @return 로그인 결과
     */
    LoginResponse login(LoginRequest request, HttpServletResponse response);

    /**
     * AccessToken을 통해 내 정보 조회
     *
     * @param authString AccessToken
     * @return 내 정보 조회 결과
     */
    GetMyInfoResponse getMyInfoByAccessToken(String authString);

    /**
     * 로그아웃 처리
     *
     * @param authString            AccessToken
     * @param httpServletRequest   HTTP 요청 (RefreshToken 조회용)
     * @param httpServletResponse  HTTP 응답 (RefreshToken 삭제용)
     * @return 로그아웃 결과
     */
    LogoutResponse logout(
            String authString,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    );

    /**
     * 토큰에서 유저 아이디 추출
     *
     * @param token JWT 토큰
     * @return 유저 아이디
     */
    String getUserFromToken(String token);

}
