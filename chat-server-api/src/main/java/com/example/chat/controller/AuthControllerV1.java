package com.example.chat.controller;

import com.example.chat.model.request.CreateUserRequest;
import com.example.chat.model.request.LoginRequest;
import com.example.chat.model.response.*;
import com.example.chat.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth API", description = "V1 Auth API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthControllerV1 {

    private final AuthService authService;

    @Operation(
            summary = "새로운 유저를 생성합니다.",
            description = "새로운 유저 생성"
    )
    @PostMapping("/create-user")
    public CreateUserResponse createUser(
            @RequestBody @Valid CreateUserRequest request) {
        return authService.createUser(request);
    }

    @Operation(
            summary = "로그인 처리",
            description = "유저의 로그인을 진행"
    )
    @PostMapping("/login")
    public LoginResponse loginResponse(
            @RequestBody @Valid LoginRequest request, HttpServletResponse httpResponse) {
        return authService.login(request, httpResponse);
    }

    @Operation(
            summary = "토큰 값을 통해 유저명 획득",
            description = "Jwt 토큰값에서 유저 ID 값을 추출합니다."
    )
    @GetMapping("/verify-token/{token}")
    public String getUserFromToken(
            @PathVariable String token) {
        return authService.getUserFromToken(token);
    }

    @Operation(
            summary = "로그인된 유저 정보 반환",
            description = "로그인된 유저의 정보를 반환합니다."
    )

    @GetMapping("/get-my-info")
    public GetMyInfoResponse getMyInfo(
            @RequestHeader("Authorization") String authString
    ) {
        return authService.getMyInfoByAccessToken(authString);
    }

    @Operation(
            summary = "로그아웃 처리",
            description = "유저의 로그아웃을 진행"
    )
    @GetMapping("/logout")
    public LogoutResponse logout(
            @RequestHeader("Authorization") String authString,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    ) {
        return authService.logout(authString, httpServletRequest, httpServletResponse);
    }

    @Operation(
            summary = "Access Token 재발급",
            description = "Refresh Token을 통해 Access Token을 재발급합니다."
    )
    @GetMapping("/reissue-access-token")
    public ReissueAccessTokenResponse reissueAccessToken(HttpServletRequest request) {
        return authService.reissueAccessToken(request);
    }
}
