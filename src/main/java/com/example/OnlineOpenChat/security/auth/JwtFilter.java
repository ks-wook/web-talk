package com.example.OnlineOpenChat.security.auth;

import com.example.OnlineOpenChat.security.CustomUserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    /**
     * Jwt 검증 필터
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        String token = JWTProvider.resolveToken(request);

        // 추출한 토큰이 유효한지 검증
        if (token != null && JWTProvider.validateToken(token)) {
            String userId = JWTProvider.getUserId(token);

            // accessToken이 유효한지만 검증
            // accessToken이 만료된 경우 별도 API 호출하여 재발급 받도록 수정 - RefreshToken 기반으로

            // 1) Principal 생성
            CustomUserPrincipal principal = new CustomUserPrincipal(userId);

            // 2) Authentication 생성
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    Collections.emptyList()
            );

            // 3) SecurityContext에 등록 -> 인증된 유저 Context 세팅
            SecurityContextHolder.getContext().setAuthentication(auth);

        }

        filterChain.doFilter(request, response);
    }
}
