package com.example.chat.config;

import com.example.chat.handler.WebSocketSessionHandler;
import com.example.chat.interceptor.WebSocketHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketSessionHandler webSocketSessionHandler;
    private final WebSocketHandshakeInterceptor webSocketHandshakeInterceptor;

    public WebSocketConfig(
            WebSocketSessionHandler webSocketSessionHandler,
            WebSocketHandshakeInterceptor webSocketHandshakeInterceptor
    ) {
        this.webSocketSessionHandler = webSocketSessionHandler;
        this.webSocketHandshakeInterceptor = webSocketHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketSessionHandler, "/ws/chat")
                .addInterceptors(webSocketHandshakeInterceptor)
                .setAllowedOrigins("*"); // production 환경에서는 도메인 제한 권장
    }
}