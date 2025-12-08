package com.example.OnlineOpenChat.config;


import com.example.OnlineOpenChat.security.auth.JwtHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
/**
 * STOMP 설정
 * 참고문서 : https://docs.spring.io/spring-framework/reference/web/websocket/stomp/enable.html
 */
public class WssConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub");

        // 구독 경로 설정
        // 참고문서 : https://docs.spring.io/spring-framework/reference/web/websocket/stomp/enable.html
        registry.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new JwtHandshakeInterceptor()); // 웹소켓 핸드쉐이크 시 인증 처리를 위한 인터셉터 등록
    }
}
