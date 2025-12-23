package com.ssafy.project.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 메시지 브로커 설정
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트가 구독(subscribe)할 주소 prefix
        registry.enableSimpleBroker("/topic");

        // 클라이언트가 메시지 보낼 때 prefix
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * 웹소켓 연결 엔드포인트
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-challenge-chat")
                .setAllowedOriginPatterns("http://localhost:5500", "http://localhost:8080")
                .withSockJS(); // ⭐⭐⭐ 이 줄이 핵심
    }

}
