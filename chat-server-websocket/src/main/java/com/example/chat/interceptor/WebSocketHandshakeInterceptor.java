package com.example.chat.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        try {
            // 웹소켓 연결 요청 쿼리로부터 UserId 추출
            // ws://localhost:8080/chat?userId=123
            URI uri = request.getURI();
            String query = uri.getQuery();

            if (query != null) {
                Map<String, String> params = parseQuery(query);
                String userIdStr = params.get("userId");

                if (userIdStr != null) {
                    try {
                        // 추출된 유저 ID값을 attributes에 저장
                        Long userId = Long.parseLong(userIdStr);
                        attributes.put("userId", userId);
                        return true;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
            }
            return false;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        if (exception != null) {
            log.error("WebSocket HandshakeInterceptor exception", exception);
        } else {
            log.error("WebSocket HandshakeInterceptor");
        }
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> result = new HashMap<>();

        String[] params = query.split("&");
        for (String param : params) {
            String[] parts = param.split("=", 2);
            if (parts.length == 2) {
                result.put(parts[0], parts[1]);
            }
        }
        return result;
    }
}
